package dev.brighten.anticheat.check.impl.premium;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.KauriVersion;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Motion", description = "motion check kind of thing.", checkType = CheckType.GENERAL,
        developer = true, planVersion = KauriVersion.ARA)
@Cancellable(cancelType = CancelType.MOVEMENT)
public class Motion extends Check {

    private int buffer;
    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(packet.isPos() && data.playerInfo.deltaXZ > 0) {
            double predXZ = (data.predictionService.predX * data.predictionService.predX)
                    + (data.predictionService.predZ * data.predictionService.predZ);

            if(data.predictionService.flag
                    && data.playerInfo.soulSandTimer.isPassed(10)
                    && data.playerInfo.lastVelocity.isPassed(5)
                    && !data.playerInfo.generalCancel
                    && !data.playerInfo.doingTeleport
                    && (data.playerInfo.deltaXZ * data.playerInfo.deltaXZ) > predXZ
                    && !data.blockInfo.collidesHorizontally) {
                if(++buffer > 15) {
                    vl++;
                    flag("deltaX=%s deltaZ=%s",
                            MathUtils.round(data.playerInfo.deltaXZ, 3), MathUtils.round(Math.sqrt(predXZ), 3));
                }
            } else buffer-= buffer > 0 ? 1.25 : 0;
            debug("(dy=%.4f) dxz=%.5f pxz=%.5f friction=%.4f key=%s collided=%s",
                    data.playerInfo.deltaY, data.playerInfo.deltaXZ, predXZ,
                    data.blockInfo.fromFriction, data.predictionService.key, data.blockInfo.collidesHorizontally);
        }
    }
}