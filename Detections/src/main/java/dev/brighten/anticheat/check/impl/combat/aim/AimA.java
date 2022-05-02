package dev.brighten.anticheat.check.impl.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;

@CheckInfo(name = "Aim (A)", description = "Ensures that yaw and pitch acceleration is legitimate.",
        checkType = CheckType.AIM, punishVL = 60, devStage = DevStage.BETA)
public class AimA extends Check {
    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(packet.isLook()) {
            float yawAccel = MathUtils.getDelta(data.playerInfo.lDeltaYaw, data.playerInfo.deltaYaw);
            float pitchAccel = MathUtils.getDelta(data.playerInfo.lDeltaPitch, data.playerInfo.deltaPitch);

            if(yawAccel < 1E-3 && pitchAccel < 1E-4
                    && (data.playerInfo.deltaPitch > 0 || yawAccel > 0)
                    && (data.moveProcessor.deltaX > 2 || data.moveProcessor.deltaY > 2)) {
                if(vl++ > 20) {
                    flag("yawAccel=%s pitchAccel=%s", yawAccel, pitchAccel);
                }
            } else vl-= vl > 0 ? 2 : 0;

            debug("yaw=" + yawAccel + " pitch=" + pitchAccel + " vl=" + vl
                    + " yd=" + data.playerInfo.deltaYaw);
        }
    }
}
