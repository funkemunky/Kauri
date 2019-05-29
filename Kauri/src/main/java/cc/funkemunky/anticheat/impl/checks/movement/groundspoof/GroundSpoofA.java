package cc.funkemunky.anticheat.impl.checks.movement.groundspoof;

import cc.funkemunky.anticheat.api.checks.*;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Verbose;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import lombok.val;
import org.bukkit.event.Event;

@CheckInfo(name = "GroundSpoofA (Type A)", type = CheckType.MOVEMENT, cancelType = CancelType.MOTION)
@Init
@Packets(packets = {Packet.Client.POSITION, Packet.Client.POSITION_LOOK, Packet.Client.LEGACY_POSITION_LOOK, Packet.Client.LEGACY_POSITION})
public class GroundSpoofA extends Check {

    private Verbose verbose = new Verbose();

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        if(getData().isGeneralCancel()
                || move.getLiquidTicks() > 0
                || move.getClimbTicks() > 0
                || move.getWebTicks() > 0
                || move.isOnHalfBlock()
                || move.isBlocksOnTop())
            return;

        if(move.isServerOnGround() != move.isClientOnGround()) {
            if(verbose.flag(7, 400L)) {
                flag("client=" + move.isClientOnGround() + " server=" + move.isServerOnGround(), true, true, verbose.getVerbose() > 13 ? AlertTier.HIGH : AlertTier.LIKELY);
            }
        } else verbose.deduct();
        debug("client=" + move.isClientOnGround() + " server=" + move.isServerOnGround() + " vl=" + verbose.getVerbose() + " dy=" + move.getDeltaY() + " y=" + move.getTo().getY());
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
