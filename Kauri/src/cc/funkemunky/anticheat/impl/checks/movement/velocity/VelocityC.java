package cc.funkemunky.anticheat.impl.checks.movement.velocity;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import cc.funkemunky.api.utils.*;
import lombok.val;
import lombok.var;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffectType;

@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.POSITION, Packet.Client.LEGACY_POSITION_LOOK, Packet.Client.LEGACY_POSITION})
@CheckInfo(name = "Velocity (Type C)", description = "Predicts the minimum horizontal movement someone should move when velocity is taken.", type = CheckType.VELOCITY, cancelType = CancelType.MOTION, developer = true, executable = false)
@Init
public class VelocityC extends Check {

    private double vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();
        val vel = getData().getVelocityProcessor();
        double offsetY = move.getTo().getY() - move.getFrom().getY();
        double offsetH = move.getDeltaXZ();
        double velocityH = MiscUtils.hypot(vel.getVelocityX(), vel.getVelocityZ());

        val collides = move.isBlocksNear();

        if (!move.isBlocksOnTop() && !move.isInLiquid() && vel.getLastVelocity().hasNotPassed(1) && velocityH > 0.45 && !move.isInsideBlock() && !move.isBlocksNear()) {
            double ratio = offsetH / velocityH;
            double threshold = 0.62;
            if (ratio < threshold) {
                if ((vl += 1.1) >= 8.0) {
                    flag("velocity: " + Math.round(ratio * 100.0) + "%", true, true);
                }
            } else {
                vl -= 0.4;
            }
            debug(Color.Green + "RATIO: " + ratio + "/" + threshold + " VL" + vl);
        }
        if(vel.getLastVelocity().hasNotPassed(10)) {
            //debug("VEL: " + velocityH + " IB: " + move.isInsideBlock() + ", " + collides + ", " + move.isClientOnGround() + ", " + MathUtils.round(offsetY, 4) + ", " + move.isBlocksOnTop());
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
