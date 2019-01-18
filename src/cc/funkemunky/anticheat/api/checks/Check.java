package cc.funkemunky.anticheat.api.checks;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.anticheat.api.utils.ConfigSetting;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Verbose;
import cc.funkemunky.api.event.system.Listener;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
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
@Init
public abstract class Check implements Listener, org.bukkit.event.Listener {
    private String name;
    private CancelType cancelType;
    private PlayerData data;
    private int vl, maxVL;
    private boolean enabled, executable, cancellable;
    private Verbose lagVerbose = new Verbose();
    private long lastAlert, alertDelay = 0;
    private String execCommand;
    private Map<String, Object> settings = new HashMap<>();

    @ConfigSetting
    private String alertMessage = "&8[&b&lKauri&8] &f%player% &7has failed &f%check% &c(x%vl%)";

    @ConfigSetting
    private long alertsDelay = 1000;

    public Check(String name, CancelType cancelType, int maxVL) {
        this.name = name;
        this.cancelType = cancelType;
        this.vl = 0;
        this.maxVL = maxVL;

        enabled = executable = true;

        alertMessage = alertMessage.replaceAll("%check%", getName());

        loadFromConfig();
    }

    public Check(String name, CancelType cancelType, PlayerData data, int maxVL) {
        this.name = name;
        this.cancelType = cancelType;
        this.data = data;
        this.vl = 0;
        this.maxVL = maxVL;

        enabled = executable = true;

        alertMessage = alertMessage.replaceAll("%check%", getName());
        loadFromConfig();
    }

    protected void flag(String information, boolean cancel, boolean ban) {
        if (data.getLagTicks() == 0 || lagVerbose.flag(4, 500L)) {
            if (vl++ > maxVL && executable && ban) {
                MiscUtils.allahAkbar(getData().getPlayer());
                new BukkitRunnable() {
                    public void run() {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), execCommand.replaceAll("%player%", getData().getPlayer().getName()));
                    }
                }.runTaskLater(Kauri.getInstance(), 30);
            }

            if (cancel && cancellable) data.setCancelType(cancelType);

            if (System.currentTimeMillis() - lastAlert > alertDelay) {
                JsonMessage message = new JsonMessage();

                message.addText(Color.translate(alertMessage.replaceAll("%player%", data.getPlayer().getName()).replaceAll("%vl%", String.valueOf(vl)).replaceAll("%info%", information))).addHoverText(Color.Gray + information);
                Bukkit.getOnlinePlayers().stream().filter(player -> player.hasPermission("fiona.alerts")).forEach(message::sendToPlayer);
                lastAlert = System.currentTimeMillis();
            }
        }
    }

    protected void loadFromConfig() {
        if (Kauri.getInstance().getConfig().get("checks." + name) != null) {
            maxVL = Kauri.getInstance().getConfig().getInt("checks." + name + ".maxVL");
            enabled = Kauri.getInstance().getConfig().getBoolean("checks." + name + ".enabled");
            executable = Kauri.getInstance().getConfig().getBoolean("checks." + name + ".executable");
            cancellable = Kauri.getInstance().getConfig().getBoolean("checks." + name + ".cancellable");
            alertDelay = Kauri.getInstance().getConfig().getLong("checks." + name + ".alertDelay");

            for (String key : settings.keySet()) {
                String path = "checks." + name + ".settings." + key;
                if (Kauri.getInstance().getConfig().get(path) != null) {
                    settings.put(key, Kauri.getInstance().getConfig().get(path));
                } else {
                    Kauri.getInstance().getConfig().set("checks." + name + ".settings." + key, settings.get(key));
                    Kauri.getInstance().saveConfig();
                }
            }
        } else {
            Kauri.getInstance().getConfig().set("checks." + name + ".maxVL", maxVL);
            Kauri.getInstance().getConfig().set("checks." + name + ".enabled", enabled);
            Kauri.getInstance().getConfig().set("checks." + name + ".executable", executable);
            Kauri.getInstance().getConfig().set("checks." + name + ".cancellable", cancellable);
            Kauri.getInstance().getConfig().set("checks." + name + ".alertDelay", alertDelay);

            for (String key : settings.keySet()) {
                Kauri.getInstance().getConfig().set("checks." + name + ".settings." + key, settings.get(key));
            }
            Kauri.getInstance().saveConfig();
        }

        execCommand = Kauri.getInstance().getConfig().getString("executableCommand").replaceAll("%check%", getName());
    }

    public void debug(String debugString) {
        Kauri.getInstance().getDataManager().getDataObjects().stream()
                .filter(dData -> dData.getDebuggingPlayer() != null && dData.getDebuggingCheck() != null && dData.getDebuggingCheck().getName().equals(name) && dData.getDebuggingPlayer().equals(data.getUuid()))
                .forEach(dData -> dData.getPlayer().sendMessage(Color.translate("&8[&cDebug&8] &7" + debugString)));
    }

    public abstract void onPacket(Object packet, String packetType, long timeStamp);

    public abstract void onBukkitEvent(Event event);
}
