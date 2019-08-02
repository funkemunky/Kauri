package cc.funkemunky.anticheat.impl.pup.crashers;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.pup.AntiPUP;
import cc.funkemunky.anticheat.api.pup.PuPType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Color;
import org.bukkit.scheduler.BukkitRunnable;

@Packets(packets = {Packet.Client.FLYING, Packet.Client.POSITION, Packet.Client.POSITION_LOOK, Packet.Client.LOOK})
public class MorePackets extends AntiPUP {
    public MorePackets(String name, PuPType type, boolean enabled) {
        super(name, type, enabled);
    }

    @Setting(name = "kick.enabled")
    private boolean kick = true;

    @Setting(name = "kick.message")
    private String kickMessage = "You are sending too many packets.";

    private long lastTimeStamp;

    private int vl;
    private boolean kicked = false;

    @Override
    public boolean onPacket(Object packet, String packetType, long timeStamp) {

        long delta = timeStamp - lastTimeStamp;
        if (delta < 20 && timeStamp - Kauri.getInstance().getLastTPS() < 150) {
            if(vl++ > 80) {
                if (vl > 200) {
                    if (!kicked) {
                        new BukkitRunnable() {
                            public void run() {
                                kicked = true;
                                getData().getPlayer().kickPlayer(Color.translate(kickMessage));
                            }
                        }.runTask(Kauri.getInstance());
                    }
                    lastTimeStamp = timeStamp;
                    return true;
                }
                return true;
            }
        } else vl = 0;
        lastTimeStamp = timeStamp;
        return false;
    }
}
