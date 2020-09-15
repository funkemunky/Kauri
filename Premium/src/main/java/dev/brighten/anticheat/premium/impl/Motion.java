package dev.brighten.anticheat.premium.impl;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Motion", description = "motion check kind of thing.", checkType = CheckType.GENERAL,
        developer = true, enabled = false)
@Cancellable(cancelType = CancelType.MOVEMENT)
public class Motion extends Check {

    private int buffer;
    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(packet.isPos()) {
            double predXZ = Math.hypot(data.predictionService.predX, data.predictionService.predZ);

            if(data.predictionService.flag
                    && data.playerInfo.soulSandTimer.hasPassed(10)
                    && !data.playerInfo.generalCancel
                    && data.playerInfo.deltaXZ > predXZ
                    && !data.blockInfo.collidesHorizontally) {
                if(++buffer > 15) {
                    vl++;
                    flag("deltaX=%v deltaZ=%v",
                            MathUtils.round(data.playerInfo.deltaXZ, 3), MathUtils.round(predXZ, 3));
                }
            } else buffer-= buffer > 0 ? 1.25 : 0;
            debug("deltaX=" + data.playerInfo.deltaXZ + " deltaZ=" + predXZ + " key=" + data.predictionService.key
                    + " collided=" + data.blockInfo.collidesHorizontally);
        }
    }
}
