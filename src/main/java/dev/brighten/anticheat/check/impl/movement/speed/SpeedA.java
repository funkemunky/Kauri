package dev.brighten.anticheat.check.impl.movement.speed;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;

@CheckInfo(name = "Speed (Type A)", description = "A speed check")
public class SpeedA extends Check {

    private long moveTicks, keyTicks;

    @Packet
    public void onPacket(WrappedInFlyingPacket packet) {
        double deltaXZ = data.playerInfo.deltaXZ;
        if(!packet.isPos() || deltaXZ < 0.1 || data.playerInfo.generalCancel) {
            moveTicks = 0;
            return;
        }

        double predictedXZ = MathUtils.hypot(data.predictionService.motionX, data.predictionService.motionZ);

        if(deltaXZ > predictedXZ) {
            vl++;
            if(vl > 120) {
               // punish();
            } else if(vl > 40) {
                //flag(deltaXZ + ">-" + predictedXZ);
            }
        } else vl-= vl > 0 ? 0.5 : 0;

        moveTicks++;
        debug("x=" + data.playerInfo.deltaX + " z=" + data.playerInfo.deltaZ + " px=" + data.predictionService.motionX + ", pz=" + data.predictionService.motionZ);
    }
}
