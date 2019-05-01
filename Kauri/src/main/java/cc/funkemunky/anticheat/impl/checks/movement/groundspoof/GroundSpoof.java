package cc.funkemunky.anticheat.impl.checks.movement.groundspoof;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Verbose;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import lombok.val;
import org.bukkit.event.Event;

@CheckInfo(name = "GroundSpoof", type = CheckType.MOVEMENT, cancelType = CancelType.MOTION)
@Init
@Packets(packets = {Packet.Client.POSITION, Packet.Client.POSITION_LOOK, Packet.Client.LEGACY_POSITION_LOOK, Packet.Client.LEGACY_POSITION})
public class GroundSpoof extends Check {

    private Verbose verbose = new Verbose();

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        if(getData().isGeneralCancel()
                || getData().getLastServerPos().hasNotPassed(1)
                || move.getLiquidTicks() > 0
                || move.getClimbTicks() > 0
                || move.getWebTicks() > 0
                || move.isOnHalfBlock()
                || move.isBlocksOnTop())
            return;

        if(move.isServerOnGround() != move.isClientOnGround()) {
            if(verbose.flag(4, 400L)) {
                flag("client=" + move.isClientOnGround() + " server=" + move.isServerOnGround(), true, true);
            }
        } else verbose.deduct();
        debug("client=" + move.isClientOnGround() + " server=" + move.isServerOnGround() + " vl=" + verbose.getVerbose() + " dy=" + move.getDeltaY());
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
