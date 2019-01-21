package cc.funkemunky.anticheat.api.checks;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.anticheat.api.utils.Verbose;
import cc.funkemunky.api.event.system.Listener;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.JsonMessage;
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
    private CancelType cancelType;
    private PlayerData data;
    private int vl, maxVL;
    private boolean enabled, executable, cancellable;
    private Verbose lagVerbose = new Verbose();
    private long lastAlert;
    private String execCommand;
    private Map<String, Object> settings = new HashMap<>();
    private String alertMessage = "";

    public Check(String name, CancelType cancelType, int maxVL) {
        this.name = name;
        this.cancelType = cancelType;
        this.vl = 0;
        this.maxVL = maxVL;

        enabled = executable = true;

        alertMessage = CheckSettings.alertMessage.replaceAll("%check%", name);

        loadFromConfig();
    }

    public Check(String name, CancelType cancelType, PlayerData data, int maxVL) {
        this.name = name;
        this.cancelType = cancelType;
        this.data = data;
        this.vl = 0;
        this.maxVL = maxVL;

        enabled = executable = true;

        alertMessage = CheckSettings.alertMessage.replaceAll("%check%", name);
        loadFromConfig();
    }

    protected void flag(String information, boolean cancel, boolean ban) {
        if (data.getLastLag().hasPassed() || lagVerbose.flag(4, 500L)) {
            vl++;
            int vl = Kauri.getInstance().getLoggerManager().addViolationToLogAndSave(this, data.getUuid());
            if(!data.isLagging() && Kauri.getInstance().getTPS() > 19 && ban && vl++ > maxVL && executable) {
                new BukkitRunnable() {
                    public void run() {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), execCommand.replaceAll("%player%", getData().getPlayer().getName()));
                    }
                }.runTaskLater(Kauri.getInstance(), 30);
            }

            if (cancel && cancellable) data.setCancelType(cancelType);

            if (System.currentTimeMillis() - lastAlert > CheckSettings.alertsDelay) {
                JsonMessage message = new JsonMessage();

                message.addText(Color.translate(alertMessage.replaceAll("%player%", data.getPlayer().getName()).replaceAll("%vl%", String.valueOf(vl)).replaceAll("%info%", information))).addHoverText(Color.Gray + information);
                Bukkit.getOnlinePlayers().stream().filter(player -> player.hasPermission("fiona.alerts")).forEach(message::sendToPlayer);
                lastAlert = System.currentTimeMillis();
                Kauri.getInstance().getLoggerManager().addAlertToLog(getData(), this, "failed " + name + " VL " + vl + " [info: " + information + " lagTicks: " + getData().getLagTicks() + " transPing: " + getData().getTransPing() + "ms]");
            }
        }
    }

    protected void loadFromConfig() {
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

        execCommand = Kauri.getInstance().getConfig().getString("executableCommand").replaceAll("%check%", getName());
    }

    public void debug(String debugString) {
        Kauri.getInstance().getDataManager().getDataObjects().stream()
                .filter(dData -> dData.getDebuggingPlayer() != null && dData.getDebuggingCheck() != null && dData.getDebuggingCheck().getName().equals(name) && dData.getDebuggingPlayer().equals(data.getUuid()))
                .forEach(dData -> dData.getPlayer().sendMessage(Color.translate("&8[&cDebug&8] &7" + debugString)));
    }

    public abstract Object onPacket(Object packet, String packetType, long timeStamp);

    public abstract void onBukkitEvent(Event event);
}
