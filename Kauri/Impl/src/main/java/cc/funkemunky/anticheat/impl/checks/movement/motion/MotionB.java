package cc.funkemunky.anticheat.impl.checks.movement.motion;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.CustomLocation;
import cc.funkemunky.anticheat.api.utils.MathUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import lombok.val;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

@Init
@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.POSITION})
@CheckInfo(name = "Motion (Type B)", description = "Looks for strafe control.", type = CheckType.MOTION)
public class MotionB extends Check {

    private float lastDirDelta, vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        if(move.getDeltaXZ() == 0) return;

        /*

        float[] direction = MathUtils.getRotationFromPosition(move.getFrom(), move.getTo());

        float dirDelta = MathUtils.getDistanceBetweenAngles(direction[0], lastDirection[0]);

        float delta = cc.funkemunky.api.utils.MathUtils.getDelta(move.getYawDelta(), dirDelta);

        if(getData().getLastAttack().hasNotPassed(0) && move.getDeltaXZ() > 0.22) {
            if(delta > 15) {
                debug(Color.Green + "Flag");
            }

            debug("yawDelta=" + move.getYawDelta() +" dirDelta=" + dirDelta + " delta=" + cc.funkemunky.api.utils.MathUtils.round(delta, 1));
        }


        lastDirection = direction;
        */

        float[] direction = MathUtils.getRotationFromPosition(move.getFrom(), move.getTo());
        Vector dir = move.getTo().toLocation(getData().getPlayer().getWorld()).getDirection();
        float yaw = (float)(Math.atan2(dir.getZ(), dir.getX()) * 180.0 / 3.141592653589793) - 90.0f;

        float delta = MathUtils.getDistanceBetweenAngles(yaw, direction[0]);
        float difference = cc.funkemunky.api.utils.MathUtils.getDelta(delta, lastDirDelta);

        if(delta > 10 && difference < 10 && difference > 3 && getData().getLastAttack().hasNotPassed(1)) {
            if(vl++ > 8) {
                flag("delta=" + delta + " diff=" + difference, true, true, vl > 15 ? AlertTier.HIGH : AlertTier.LIKELY);
            }
        } else vl-= vl > 0 ? 0.2f : 0;
        debug("delta=" + delta + " diff=" + difference + " vl=" + vl);
        lastDirDelta = delta;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }

}
