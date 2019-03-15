package cc.funkemunky.anticheat.api.pup;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.data.PlayerData;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public abstract class AntiPUP implements Listener {

    @Getter
    private String name;

    @Getter
    @Setter
    private PlayerData data;

    @Getter
    @Setter
    private boolean enabled;

    public AntiPUP(String name, boolean enabled) {
        this.name = name;
        this.enabled = enabled;
    }

    public abstract boolean onPacket(Object packet, String packetType, long timeStamp);

    public void setEnabled(boolean enabled) {
        if (this.enabled = enabled) {
            Bukkit.getPluginManager().registerEvents(this, Kauri.getInstance());
        } else {
            HandlerList.unregisterAll(this);
        }
    }
}
