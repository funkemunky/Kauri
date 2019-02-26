package cc.funkemunky.anticheat.api.checks;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.anticheat.api.utils.Verbose;
import cc.funkemunky.api.event.system.Listener;
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
public abstract class Check implements Listener, org.bukkit.event.Listener {
    private String name;
    private CheckType type;
    private String description;
    private CancelType cancelType;
    private PlayerData data;
    private int maxVL;
    private boolean enabled, executable, cancellable, developer;
    private Verbose lagVerbose = new Verbose();
    private long lastAlert;
    private List<String> execCommand = new ArrayList<>();
    private Map<String, Object> settings = new HashMap<>();
    private String alertMessage = "";
    private int vl;
    private ProtocolVersion minimum, maximum;

    public Check(String name, String description, CheckType type, CancelType cancelType, int maxVL, boolean enabled, boolean executable, boolean cancellable) {
        this.name = name;
        this.description = description;
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
            if(ban) {
                vl++;
                Kauri.getInstance().getLoggerManager().addViolation(data.getUuid(), this);
                Kauri.getInstance().getStatsManager().addFlag();
            }
            if (vl > maxVL && executable && ban && !getData().isBanned()) {
                getData().setBanned(true);
                new BukkitRunnable() {
                    public void run() {
                        execCommand.forEach(cmd -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replaceAll("%player%", getData().getPlayer().getName()).replaceAll("%check%", getName())));
                    }
                }.runTaskLater(Kauri.getInstance(), 30);
                Kauri.getInstance().getLoggerManager().addBan(data.getUuid(), this);
            }

            data.getLastFlag().reset();

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
            Kauri.getInstance().getConfig().getStringList("checks." + name + ".executableCommands").forEach(cmd -> {
                if(cmd.equals("%global%")) {
                    execCommand.addAll(CheckSettings.executableCommand);
                } else {
                    execCommand.add(cmd);
                }
            });
        } else {
            Kauri.getInstance().getConfig().set("checks." + name + ".maxVL", maxVL);
            Kauri.getInstance().getConfig().set("checks." + name + ".enabled", enabled);
            Kauri.getInstance().getConfig().set("checks." + name + ".executable", executable);
            Kauri.getInstance().getConfig().set("checks." + name + ".cancellable", cancellable);
            Kauri.getInstance().getConfig().set("checks." + name + ".executableCommands", Collections.singletonList("%global%"));

            Kauri.getInstance().saveConfig();
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
