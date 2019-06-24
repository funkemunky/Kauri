package cc.funkemunky.anticheat.impl.pup.crashers;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.pup.AntiPUP;
import cc.funkemunky.anticheat.api.pup.PuPType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Color;
import org.bukkit.Bukkit;

@Packets(packets = {Packet.Client.FLYING, Packet.Client.POSITION, Packet.Client.POSITION_LOOK, Packet.Client.LOOK, Packet.Client.LEGACY_LOOK, Packet.Client.LEGACY_POSITION_LOOK, Packet.Client.LEGACY_POSITION})
public class MorePackets extends AntiPUP {
    public MorePackets(String name, PuPType type, boolean enabled) {
        super(name, type, enabled);
    }

    @Setting(name = "kick.enabled")
    private boolean kick = false;

    @Setting(name = "kick.message")
    private String kickMessage = "You are sending too many packets.";

    @Setting(name = "maxVL")
    private int maxVL = 40;

    private long lastTimeStamp;

    private int vl;

    @Override
    public boolean onPacket(Object packet, String packetType, long timeStamp) {

        if (timeStamp < lastTimeStamp + 30) {
            if (vl++ > maxVL) {
                if (kick)
                    Bukkit.getScheduler().scheduleSyncDelayedTask(Kauri.getInstance(), () -> getData().getPlayer().kickPlayer(Color.translate(kickMessage)));
                lastTimeStamp = timeStamp;
                return true;
            }
        } else vl -= vl > 0 ? 2 : 0;

        lastTimeStamp = timeStamp;
        return false;
    }
}
