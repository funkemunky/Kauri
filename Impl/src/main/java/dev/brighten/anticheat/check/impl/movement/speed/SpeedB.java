package dev.brighten.anticheat.check.impl.movement.speed;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;

@CheckInfo(name = "Speed (B)", description = "Predicts the motion of a player accurately.", developer = true,
        executable = false, punishVL = 150)
public class SpeedB extends Check {

    private int moveTicks;
    private double lastPredXZ;
    @Packet
    public void onPacket(WrappedInFlyingPacket packet) {
        if(packet.isPos() && data.playerInfo.deltaXZ > 0) {
            double predXZ = MathUtils
                    .hypot(data.predictionService.predX, data.predictionService.predZ);
            if(moveTicks < 40) {
                moveTicks++; //jank way to prevent false positives until a player moves a bit but works.
            } else {
                float pAccel = (float) Math.abs(predXZ - lastPredXZ), accel = Math.abs(data.playerInfo.deltaXZ - data.playerInfo.lDeltaXZ);

                if(MathUtils.getDelta(pAccel, accel) > 1E-5
                        && !data.playerInfo.generalCancel
                        && !data.blockInfo.onClimbable
                        && !data.blockInfo.inLiquid
                        && !data.playerInfo.collidesHorizontally) {
                    if(vl++ > 2 || MathUtils.getDelta(pAccel, accel) > 0.4f) {
                        flag("accel=" + accel + " p=" + pAccel);
                    }
                } else vl-= vl > 0 ? 0.2f : 0;
                debug("accel=" + accel + " pAccel=" + pAccel);
            }
            lastPredXZ = predXZ;
        }
    }
}
