package cc.funkemunky.anticheat.impl.checks.combat.aimassist;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MathUtils;
import lombok.val;
import org.bukkit.event.Event;

@CheckInfo(name = "Aim (Type P)", description = "Checks for consistent angling.", type = CheckType.AIM, maxVL = 75)
@Init
@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.LEGACY_POSITION_LOOK})
public class AimP extends Check {

    private double lastOffset, lastAcceleration, lastGCD;
    private int vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        if(getData().getTarget() != null && !getData().getTarget().isDead() && getData().getTarget().getWorld().getUID().equals(getData().getPlayer().getWorld().getUID())) {
            val entityLoc = getData().getEntityPastLocation().getPreviousLocation(getData().getTransPing());
            val world = getData().getPlayer().getWorld();
            val offset = MathUtils.getOffsetFromLocation(move.getTo().toLocation(world), entityLoc.toLocation(world))[0];
            /*val acceleration = offset - lastOffset;
            val deltaAccel = acceleration - lastAcceleration;

            if(deltaAccel >= 0) {
                debug(Color.Green + "VL=" + vl++);
            } else vl-= vl > 0 ? 1 : 0;

            debug("offset=" + MathUtils.round(offset, 3) + " accel=" + MathUtils.round(acceleration, 6) + " deltaAccel=" + deltaAccel);

            lastAcceleration = acceleration;*/

            val multiply = 16777216L;
            val yawFromOffset = MathUtils.getDelta(MathUtils.yawTo180D(offset), MathUtils.yawTo180F(move.getTo().getYaw()));
            val yawDelta = move.getYawDelta();
            val gcd = MiscUtils.gcd((long) (yawDelta * multiply), (long) yawFromOffset);
            val delta = MathUtils.getDelta((float) gcd, (float) lastGCD);
            if(delta < 2E7) {
                if(vl++ > 30) {
                    flag("delta=" + delta, true, false, AlertTier.HIGH);
                }

                debug(Color.Green + " vl=" + vl);
            } else vl-= vl > 0 ? 4 : 0;

            debug("gcd=" + gcd + " test=" + (gcd/(double)multiply) + " yaw=" + move.getYawDelta() + " offset=" + offset);

            lastOffset = offset;
            lastGCD = gcd;
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
