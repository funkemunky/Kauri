package dev.brighten.anticheat.check.impl.movement.general;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Motion", description = "motion check kind of thing.", checkType = CheckType.GENERAL, developer = true)
public class Motion extends Check {

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(packet.isPos()) {
            double deltaX = MathUtils.getDelta(data.predictionService.predX, data.playerInfo.deltaX),
                    deltaZ = MathUtils.getDelta(data.predictionService.predZ, data.playerInfo.deltaZ);

            if(data.predictionService.flag && !data.playerInfo.generalCancel && !data.blockInfo.collidesHorizontally) {
                vl++;
                if(vl > 24) {
                    flag("deltaX=%1 deltaZ=%2",
                            MathUtils.round(deltaX, 3), MathUtils.round(deltaZ, 3));
                }
            } else vl-= vl > 0 ? 1.25 : 0;
            debug("deltaX=" + deltaX + " deltaZ=" + deltaZ + " key=" + data.predictionService.key
                    + " collided=" + data.predictionService.collidedHorizontally);
        }
    }
}
