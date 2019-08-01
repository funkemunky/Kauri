package cc.funkemunky.anticheat.api.checks;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.anticheat.api.event.PlayerCancelEvent;
import cc.funkemunky.anticheat.api.event.PlayerCheatEvent;
import cc.funkemunky.anticheat.impl.config.CheckSettings;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.JsonMessage;
import cc.funkemunky.api.utils.MiscUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

@Getter
@Setter
public abstract class Check {
    private String name;
    private CheckType type;
    private String description;
    private CancelType cancelType;
    private PlayerData data;
    private int maxVL, banWaveThreshold;
    private boolean enabled, executable, cancellable, developer, isBanWave;
    private long lastAlert;
    private List<String> execCommand = new ArrayList<>();
    private Map<String, Object> settings = new HashMap<>();
    private String alertMessage = "";
    private int vl = 0;
    private ProtocolVersion minimum, maximum;
    private List<String> packets = new ArrayList<>();
    private List<Class> events = new ArrayList<>();

    public Check() {
        alertMessage = CheckSettings.alertMessage;
    }

    protected void flag(String information, boolean cancel, boolean ban, AlertTier tier) {
        Kauri.getInstance().getExecutorService().execute(() -> {
            if(Kauri.getInstance().getTps() > CheckSettings.tpsThreshold && System.currentTimeMillis() - Kauri.getInstance().getLastTPS() < 100) {
                PlayerCheatEvent event = new PlayerCheatEvent(getData().getPlayer(), this);

                Bukkit.getPluginManager().callEvent(event);

                if(!event.isCancelled()) {
                    data.getLastFlag().reset();
                    final AlertTier alertTier = getData().isLagging() ? AlertTier.getByValue(tier.getPriority() - 2) : tier;

                    long timeStamp = System.currentTimeMillis();
                    if (cancel && cancellable) {
                        PlayerCancelEvent cancelEvent = new PlayerCancelEvent(getData().getPlayer(), this, cancelType);

                        Bukkit.getPluginManager().callEvent(cancelEvent);

                        if(!cancelEvent.isCancelled()) {
                            getData().setCancelType(cancelEvent.getType());
                        }
                    }

                    if (ban) {
                        if (data.getLastLag().hasPassed(8)) {
                            vl++;
                            Kauri.getInstance().getStatsManager().addFlag();
                        }
                        Kauri.getInstance().getLoggerManager().addViolation(data, this, information, tier);
                    }
                    if (alertTier.equals(AlertTier.CERTAIN) || (vl > maxVL && executable && ban && !developer && !getData().isBanned())) {
                        banUser();
                    }

                    if((timeStamp - data.getLastFlagTimestamp()) > 5) {
                        JsonMessage message = new JsonMessage();
                        if (timeStamp - lastAlert > Math.max(100, CheckSettings.alertsDelay)) {
                            if (!developer) {
                                message.addText(Color.translate(alertMessage.replace("%prefix%", CheckSettings.alertPrefix).replace("%check%", getName()).replace("%player%", data.getPlayer().getName()).replace("%vl%", String.valueOf(vl)).replace("%info%", information).replace("%chance%", alertTier.getName()))).addHoverText(Color.Gray + information);

                                Kauri.getInstance().getCheckManager().getAlerts().stream().filter(data2 -> data2.getAlertTier().getPriority() <= alertTier.getPriority()).forEach(data2 -> message.sendToPlayer(data2.getPlayer()));
                                if (CheckSettings.printToConsole) {
                                    MiscUtils.printToConsole(alertMessage.replace("%check%", (developer ? Color.Red + Color.Italics : "") + getName()).replace("%prefix%", CheckSettings.alertPrefix).replace("%check%", getName()).replace("%player%", data.getPlayer().getName()).replace("%vl%", String.valueOf(vl)).replace("%info%", information).replace("%chance%", alertTier.getName()));
                                }
                            } else {
                                message.addText(Color.translate(alertMessage.replace("%prefix%", CheckSettings.devAlertPrefix).replace("%check%", Color.Red + Color.Italics + getName()).replace("%player%", data.getPlayer().getName()).replace("%vl%", "N/A").replace("%chance%", alertTier.getName()).replace("%info%", information))).addHoverText(Color.Gray + information);

                                Kauri.getInstance().getCheckManager().getDevAlerts().forEach(data -> message.sendToPlayer(data.getPlayer()));
                            }
                            if (CheckSettings.testMode && !data.isAlertsEnabled()) {
                                message.sendToPlayer(data.getPlayer());
                            }
                            lastAlert = timeStamp;
                        }
                    }
                    data.setLastFlagTimestamp(timeStamp);
                }
            }
        });
    }
    private void banUser() {
        if(!getData().isBanned() && executable) {
            getData().setBanned(true);
            Kauri.getInstance().getStatsManager().addBan();
            new BukkitRunnable() {
                public void run() {
                    vl = 0;
                    execCommand.forEach(cmd -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", getData().getPlayer().getName()).replace("%check%", getName())));
                }
            }.runTaskLater(Kauri.getInstance(), 10);
            if (CheckSettings.broadcastEnabled)
                Bukkit.broadcastMessage(Color.translate(CheckSettings.broadcastMessage.replace("%player%", getData().getPlayer().getName())));
            Kauri.getInstance().getLoggerManager().addBan(data.getUuid(), this);
        }
    }

    public void loadFromConfig() {
        LinkedHashMap<String, Object> values = new LinkedHashMap<>();
        String path = "checks." + name;
        values.put(path + ".maxVL", maxVL);
        values.put(path + ".enabled", enabled);
        values.put(path + ".executable", executable);
        values.put(path + ".cancellable", cancellable);
        values.put(path + ".executableCommands", Collections.singletonList("%global%"));
        values.put(path + ".banWave.enabled", isBanWave);
        values.put(path + ".banWave.threshold", banWaveThreshold);

        values.keySet().stream().filter(key -> Kauri.getInstance().getConfig().get(key) == null).forEach(key -> {
            Kauri.getInstance().getConfig().set(key, values.get(key));
            Kauri.getInstance().saveConfig();
        });

        if (Kauri.getInstance().getConfig().get("checks." + name) != null) {
            maxVL = Kauri.getInstance().getConfig().getInt(path + ".maxVL");
            enabled = Kauri.getInstance().getConfig().getBoolean(path + ".enabled");
            executable = Kauri.getInstance().getConfig().getBoolean(path + ".executable");
            cancellable = Kauri.getInstance().getConfig().getBoolean(path + ".cancellable");
            Kauri.getInstance().getConfig().getStringList(path + ".executableCommands").forEach(cmd -> {
                if (cmd.equals("%global%")) {
                    execCommand.addAll(CheckSettings.executableCommand);
                } else {
                    execCommand.add(cmd);
                }
            });
            isBanWave = Kauri.getInstance().getConfig().getBoolean(path + ".banWave.enabled");
            banWaveThreshold = Kauri.getInstance().getConfig().getInt(path + ".banWave.threshold");
        }
    }

    public void debug(String debugString) {

    }

    public abstract void onPacket(Object packet, String packetType, long timeStamp);

    public abstract void onBukkitEvent(Event event);
}
