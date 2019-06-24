package cc.funkemunky.anticheat.impl.pup.bot;

import cc.funkemunky.anticheat.api.pup.AntiPUP;
import cc.funkemunky.anticheat.api.pup.PuPType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import org.bukkit.Bukkit;

@Packets(packets = {Packet.Client.FLYING})
public class WorldLoader extends AntiPUP {
    public WorldLoader(String name, PuPType type, boolean enabled) {
        super(name, type, enabled);
    }

    private int ticks;

    @Override
    public boolean onPacket(Object packet, String packetType, long timeStamp) {
        if(!Atlas.getInstance().getBlockBoxManager().getBlockBox().isChunkLoaded(getData().getPlayer().getLocation())) {
            if(ticks++ > 20) {
                if(ticks % 5 == 0) {
                    Bukkit.broadcastMessage(getData().getPlayer().getName());
                }
            }
        }
        return false;
    }
}
