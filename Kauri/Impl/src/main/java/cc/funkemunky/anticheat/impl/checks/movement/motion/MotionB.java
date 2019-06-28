package cc.funkemunky.anticheat.impl.checks.movement.motion;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MathUtils;
import lombok.val;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

//@Init
@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.POSITION})
@CheckInfo(name = "Motion (Type B)", description = "Looks for strafe control.", type = CheckType.MOTION)
public class MotionB extends Check {

    private double groundDirection;
    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        val direction = move.getTo().toLocation(getData().getPlayer().getWorld()).getDirection().setY(0);
        val moveVector = new Vector(move.getTo().getX() - move.getFrom().getX(), 0, move.getTo().getZ() - move.getFrom().getZ());

        val delta = direction.distance(moveVector);

        if(move.getAirTicks() <= 3) {
            groundDirection = delta;
        } else if(!move.isServerOnGround()) {
            if(MathUtils.getDelta(delta, groundDirection) > 0.26 && move.getYawDelta() < 5) {
                flag(delta + ">-" + groundDirection, true, true, AlertTier.LIKELY);
            }
            debug(delta + ", " + groundDirection);
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
