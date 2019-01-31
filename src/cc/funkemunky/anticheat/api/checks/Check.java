package cc.funkemunky.anticheat.api.checks;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.anticheat.api.utils.Verbose;
import cc.funkemunky.api.event.system.Listener;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.JsonMessage;
import cc.funkemunky.api.utils.MiscUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public abstract class Check implements Listener, org.bukkit.event.Listener {
    private String name;
    private CheckType type;
    private CancelType cancelType;
    private PlayerData data;
    private int maxVL;
    private boolean enabled, executable, cancellable, developer;
    private Verbose lagVerbose = new Verbose();
    private long lastAlert;
    private String execCommand;
    private Map<String, Object> settings = new HashMap<>();
    private String alertMessage = "";

    public Check(String name, CheckType type, CancelType cancelType, int maxVL) {
        this.type = type;
        this.name = name;
        this.cancelType = cancelType;
        this.maxVL = maxVL;

        enabled = executable = true;

        developer = false;

        alertMessage = CheckSettings.alertMessage.replaceAll("%check%", name);

        loadFromConfig();
    }

    public Check(String name, CheckType type, CancelType cancelType, PlayerData data, int maxVL) {
        this.name = name;
        this.type = type;
        this.cancelType = cancelType;
        this.data = data;
        this.maxVL = maxVL;

        enabled = executable = cancellable = true;

        developer = false;

        alertMessage = CheckSettings.alertMessage.replaceAll("%check%", name);
        loadFromConfig();
    }

    public Check(String name, CheckType type, CancelType cancelType, int maxVL, boolean enabled, boolean executable, boolean cancellable) {
        this.name = name;
        this.type = type;
        this.cancelType = cancelType;
        this.maxVL = maxVL;
        this.enabled = enabled;
        this.executable = executable;
        this.cancellable = cancellable;

        developer = false;

        alertMessage = CheckSettings.alertMessage.replaceAll("%check%", name);
        loadFromConfig();
    }

    protected void flag(String information, boolean cancel, boolean ban) {
        if (data.getLastLag().hasPassed() || lagVerbose.flag(4, 500L)) {
            int vl = Kauri.getInstance().getLoggerManager().addAndGetViolation(data.getUuid(), this);
            if (vl > maxVL && executable && ban && !Kauri.getInstance().getStatsManager().isPlayerBanned(data.getUuid())) {
                if(CheckSettings.broadcastEnabled) {
                    new BukkitRunnable() {
                        public void run() {
                            Bukkit.broadcastMessage(Color.translate(CheckSettings.broadcastMessage));
                        }
                    }.runTask(Kauri.getInstance());
                }
                new BukkitRunnable() {
                    public void run() {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), execCommand.replaceAll("%player%", getData().getPlayer().getName()));
                    }
                }.runTaskLater(Kauri.getInstance(), 10);
                Kauri.getInstance().getLoggerManager().addBan(data.getUuid(), this);
            }

            Kauri.getInstance().getStatsManager().addFlag();

            if (cancel && cancellable) data.setCancelType(cancelType);

            if (System.currentTimeMillis() - lastAlert > CheckSettings.alertsDelay) {
                JsonMessage message = new JsonMessage();

                message.addText(Color.translate(alertMessage.replaceAll("%player%", data.getPlayer().getName()).replaceAll("%vl%", String.valueOf(vl)).replaceAll("%info%", information))).addHoverText(Color.Gray + information);
                Kauri.getInstance().getDataManager().getDataObjects().values().stream().filter(PlayerData::isAlertsEnabled).forEach(data -> message.sendToPlayer(data.getPlayer()));
                lastAlert = System.currentTimeMillis();
            }

            if(CheckSettings.testMode && !data.isAlertsEnabled()) {
                JsonMessage message = new JsonMessage();

                message.addText(Color.translate(alertMessage.replaceAll("%player%", data.getPlayer().getName()).replaceAll("%vl%", String.valueOf(vl)).replaceAll("%info%", information))).addHoverText(Color.Gray + information);
                message.sendToPlayer(data.getPlayer());
            }

            if(CheckSettings.printToConsole) {
                MiscUtils.printToConsole(alertMessage.replaceAll("%player%", data.getPlayer().getName()).replaceAll("%vl%", String.valueOf(vl)));
            }
        }
    }

    private void loadFromConfig() {
        if (Kauri.getInstance().getConfig().get("checks." + name) != null) {
            maxVL = Kauri.getInstance().getConfig().getInt("checks." + name + ".maxVL");
            enabled = Kauri.getInstance().getConfig().getBoolean("checks." + name + ".enabled");
            executable = Kauri.getInstance().getConfig().getBoolean("checks." + name + ".executable");
            cancellable = Kauri.getInstance().getConfig().getBoolean("checks." + name + ".cancellable");
        } else {
            Kauri.getInstance().getConfig().set("checks." + name + ".maxVL", maxVL);
            Kauri.getInstance().getConfig().set("checks." + name + ".enabled", enabled);
            Kauri.getInstance().getConfig().set("checks." + name + ".executable", executable);
            Kauri.getInstance().getConfig().set("checks." + name + ".cancellable", cancellable);

            Kauri.getInstance().saveConfig();
        }

        execCommand = CheckSettings.executableCommand.replaceAll("%check%", getName());
    }

    public void debug(String debugString) {
        Kauri.getInstance().getDataManager().getDataObjects().values().stream()
                .filter(dData -> dData.getDebuggingPlayer() != null && dData.getDebuggingCheck() != null && dData.getDebuggingCheck().getName().equals(name) && dData.getDebuggingPlayer().equals(data.getUuid()))
                .forEach(dData -> dData.getPlayer().sendMessage(Color.translate("&8[&cDebug&8] &7" + debugString)));
    }

    public abstract Object onPacket(Object packet, String packetType, long timeStamp);

    public abstract void onBukkitEvent(Event event);
}
