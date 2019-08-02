package cc.funkemunky.anticheat.impl.checks.player.badpackets;

import cc.funkemunky.anticheat.api.checks.*;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Verbose;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockDigPacket;
import cc.funkemunky.api.utils.Init;
import org.bukkit.event.Event;

@CheckInfo(name = "BadPackets (Type I)", description = "Checks for instant blocks and unblocks.", type = CheckType.BADPACKETS, cancelType = CancelType.INTERACT, maxVL = 40, executable = true, maxVersion = ProtocolVersion.V1_8_9)
@Packets(packets = {Packet.Client.BLOCK_PLACE, Packet.Client.BLOCK_DIG})
@Init
public class BadPacketsI extends Check {

    private long lastBlockPlace;
    private Verbose verbose = new Verbose();

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(packetType.equals(Packet.Client.BLOCK_DIG)) {
            WrappedInBlockDigPacket dig = new WrappedInBlockDigPacket(packet, getData().getPlayer());

            if(dig.getAction().equals(WrappedInBlockDigPacket.EnumPlayerDigType.RELEASE_USE_ITEM)) {
                long delta = timeStamp - lastBlockPlace;

                if(!getData().isLagging() && getData().getLastLag().hasPassed(5) && delta == 0 && verbose.flag(4, 1000L)) {
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
