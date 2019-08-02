package cc.funkemunky.anticheat.impl.pup.crashers;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.pup.AntiPUP;
import cc.funkemunky.anticheat.api.pup.PuPType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import org.bukkit.scheduler.BukkitRunnable;

@Packets(packets = {Packet.Client.CHAT})
public class ChatSpam extends AntiPUP {
    public ChatSpam(String name, PuPType type, boolean enabled) {
        super(name, type, enabled);
    }

    private long lastMessage;
    private int vl;
    private boolean kicked = false;

    @Override
    public boolean onPacket(Object packet, String packetType, long timeStamp) {
        long delta = (timeStamp - lastMessage);
        if(delta <= 2 || (delta < 20 && vl++ > 10)) {
            vl++;
            if(vl > 100 && !kicked) {
                new BukkitRunnable() {
                    public void run() {
                        kicked = true;
                        getData().getPlayer().kickPlayer("Too many chat packets.");
                    }
                }.runTask(Kauri.getInstance());
            }
            return true;
        } else vl-= vl > 0 ? 1 : 0;
        lastMessage = timeStamp;
        return false;
    }
}
