package dev.brighten.anticheat.check.impl.movement.speed;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;

@CheckInfo(name = "Speed (Type A)", description = "A speed check")
public class SpeedA extends Check {

    private long moveTicks, keyTicks;
    private String lastKey = "";
    @Packet
    public void onPacket(WrappedInFlyingPacket packet) {
        double deltaXZ = data.playerInfo.deltaXZ;
        if(!packet.isPos() || deltaXZ < 0.1 || data.playerInfo.generalCancel) {
            moveTicks = 0;
            return;
        }

        if(!lastKey.equals(data.playerInfo.key)) {
            keyTicks = 0;
        } else keyTicks++;

        double predictedXZ = MathUtils.hypot(data.playerInfo.pDeltaX, data.playerInfo.pDeltaZ) + (moveTicks < 30 ? 0.005 : 0) + 0.001 + (keyTicks < 10 ? 0.03 : 0);

        if(deltaXZ > predictedXZ) {
            vl++;
            if(vl > 120) {
               // punish();
            } else if(vl > 40) {
                //flag(deltaXZ + ">-" + predictedXZ);
            }
        } else vl-= vl > 0 ? 0.5 : 0;

        moveTicks++;
        lastKey = data.playerInfo.key;
        debug("delta=" + deltaXZ + " predicted=" + predictedXZ + " key=" +data.playerInfo.key + " forward=" + data.playerInfo.forward + " strafe=" + data.playerInfo.strafe);
    }
}
