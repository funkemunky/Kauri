package cc.funkemunky.anticheat.api.pup;

import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.anticheat.api.utils.Setting;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Listener;

@Getter
@Setter
public abstract class AntiPUP implements Listener {

    private String name;
    private boolean enabled;
    private PlayerData data;

    public AntiPUP(String name, boolean enabled) {
        this.name = name;
        this.enabled = enabled;
    }

    public abstract boolean onPacket(Object packet, String packetType, long timeStamp);
}
