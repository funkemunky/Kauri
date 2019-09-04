package cc.funkemunky.anticheat.impl.checks.movement.step;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.POSITION})
//@cc.funkemunky.api.utils.Init
@CheckInfo(name = "Step (Type A)", description = "Checks for illegitimate collisions.", type = CheckType.STEP,
        maxVL = 50)
public class StepA extends Check {

    private float yTotal;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        debug(move.isServerOnGround() + "");
        if (getData().isGeneralCancel() && getData().takingVelocity(10) && move.isHalfBlocksAround()) return;

        if (move.isCollidesHorizontally()) {
            debug("TOTAL: " + (yTotal += move.getDeltaY()));
        } else if (yTotal % 1.0 == 0 && yTotal > 0 && !move.isBlocksOnTop()) {
            flag("total:" + yTotal, true, true, AlertTier.LIKELY);
            yTotal = 0;
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
