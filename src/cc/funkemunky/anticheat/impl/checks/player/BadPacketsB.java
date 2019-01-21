package cc.funkemunky.anticheat.impl.checks.player;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockDigPacket;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;

@Packets(packets = {Packet.Client.BLOCK_DIG, Packet.Client.BLOCK_PLACE})
public class BadPacketsB extends Check {
    public BadPacketsB(String name, CancelType cancelType, int maxVL) {
        super(name, cancelType, maxVL);
    }

    private long lastDig;
    @Override
    public Object onPacket(Object packet, String packetType, long timeStamp) {
        if(packetType.equals(Packet.Client.BLOCK_DIG)) {
            WrappedInBlockDigPacket dig = new WrappedInBlockDigPacket(packet, getData().getPlayer());

            if(dig.getAction().equals(WrappedInBlockDigPacket.EnumPlayerDigType.RELEASE_USE_ITEM)) {
                Bukkit.broadcastMessage((timeStamp - lastDig) + "");
            }
        } else {
            lastDig = timeStamp;
        }
        return packet;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
