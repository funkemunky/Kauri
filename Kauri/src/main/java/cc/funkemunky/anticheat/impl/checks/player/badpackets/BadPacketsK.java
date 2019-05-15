package cc.funkemunky.anticheat.impl.checks.player.badpackets;

import cc.funkemunky.anticheat.api.checks.*;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockDigPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockPlacePacket;
import cc.funkemunky.api.utils.Init;
import org.bukkit.event.Event;

@CheckInfo(name = "BadPackets (Type K)", description = "Checks for instant blocks and unblocks.", type = CheckType.BADPACKETS, cancelType = CancelType.INTERACT, developer = true, maxVL = 40)
@Packets(packets = {Packet.Client.BLOCK_PLACE, Packet.Client.BLOCK_DIG})
@Init
public class BadPacketsK extends Check {

    private long lastBlockPlace;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(packetType.equals(Packet.Client.BLOCK_DIG)) {
            WrappedInBlockDigPacket dig = new WrappedInBlockDigPacket(packet, getData().getPlayer());

            if(dig.getAction().equals(WrappedInBlockDigPacket.EnumPlayerDigType.RELEASE_USE_ITEM)) {
                long delta = timeStamp - lastBlockPlace;

                if(delta == 0) {
                    flag("delta=0", true, true, AlertTier.HIGH);
                }

                debug("delta=" + delta);
            }
        } else if(packetType.equalsIgnoreCase(Packet.Client.BLOCK_PLACE)) lastBlockPlace = timeStamp;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
