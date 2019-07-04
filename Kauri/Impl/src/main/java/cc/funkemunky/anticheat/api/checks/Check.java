package cc.funkemunky.anticheat.api.checks;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.anticheat.api.event.PlayerCancelEvent;
import cc.funkemunky.anticheat.api.event.PlayerCheatEvent;
import cc.funkemunky.api.event.system.Listener;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.JsonMessage;
import cc.funkemunky.api.utils.MiscUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
public abstract class Check implements Listener, org.bukkit.event.Listener {
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

    public Check() {
        alertMessage = CheckSettings.alertMessage;
    }

    protected void flag(String information, boolean cancel, boolean ban, AlertTier tier) {
        Kauri.getInstance().getCheckManager().getAlertsExecutable().execute(() -> {
            Kauri.getInstance().getProfiler().start("check:" + getName() + ":alert");
            if(Kauri.getInstance().getTps() > CheckSettings.tpsThreshold) {
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
                        Kauri.getInstance().getLoggerManager().addViolation(data.getUuid(), this);
                    }
                    if (alertTier.equals(AlertTier.CERTAIN) || (vl > maxVL && executable && ban && !developer && !getData().isBanned())) {
                        banUser();
                    }

                    if((timeStamp - data.getLastFlagTimestamp()) > 5) {
                        JsonMessage message = new JsonMessage();
                        if (timeStamp - lastAlert > CheckSettings.alertsDelay) {
                            val dataToAlert = Kauri.getInstance().getDataManager().getDataObjects().keySet().parallelStream().map(key -> Kauri.getInstance().getDataManager().getDataObjects().get(key)).filter(data -> data.isAlertsEnabled() && data.getAlertTier() != null && data.getPlayer().hasPermission("kauri.alerts")).collect(Collectors.toList());
                            if (!developer) {
                                message.addText(Color.translate(alertMessage.replace("%prefix%", CheckSettings.alertPrefix).replace("%check%", getName()).replace("%player%", data.getPlayer().getName()).replace("%vl%", String.valueOf(vl)).replace("%info%", information).replace("%chance%", alertTier.getName()))).addHoverText(Color.Gray + information);

                                dataToAlert.stream().filter(data2 -> data2.getAlertTier().getPriority() <= alertTier.getPriority()).forEach(data2 -> message.sendToPlayer(data2.getPlayer()));
                                if (CheckSettings.printToConsole) {
                                    MiscUtils.printToConsole(alertMessage.replace("%check%", (developer ? Color.Red + Color.Italics : "") + getName()).replace("%prefix%", CheckSettings.alertPrefix).replace("%check%", getName()).replace("%player%", data.getPlayer().getName()).replace("%vl%", String.valueOf(vl)).replace("%info%", information).replace("%chance%", alertTier.getName()));
                                }
                            } else {
                                message.addText(Color.translate(alertMessage.replace("%prefix%", CheckSettings.devAlertPrefix).replace("%check%", Color.Red + Color.Italics + getName()).replace("%player%", data.getPlayer().getName()).replace("%vl%", "N/A").replace("%chance%", alertTier.getName()).replace("%info%", information))).addHoverText(Color.Gray + information);

                                dataToAlert.stream().filter(PlayerData::isDeveloperAlerts).forEach(data -> message.sendToPlayer(data.getPlayer()));
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
            Kauri.getInstance().getProfiler().stop("check:" + getName() + ":alert");
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
                    getData().setBanned(false);
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
        Kauri.getInstance().getDataManager().getDataObjects().values().stream()
                .filter(dData -> dData.getDebuggingPlayer() != null && dData.getDebuggingCheck() != null && dData.getDebuggingCheck().getName().equals(name) && dData.getDebuggingPlayer().equals(data.getUuid()))
                .forEach(dData -> dData.getPlayer().sendMessage(Color.translate("&8[&cDebug&8] &7" + debugString)));
    }

    public abstract void onPacket(Object packet, String packetType, long timeStamp);

    public abstract void onBukkitEvent(Event event);
}
