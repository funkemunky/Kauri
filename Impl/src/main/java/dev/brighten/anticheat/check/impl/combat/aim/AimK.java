package dev.brighten.anticheat.check.impl.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Aim (K)", description = "Elevated's vape aimassist check.", checkType = CheckType.AIM,
        punishVL = 200)
public class AimK extends Check {

    private float lastYawDelta, lastPitchDelta, lastYawDifference, lastPitchDifference;
    private int verbose;

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(!packet.isLook()) return;

        float outYaw = data.playerInfo.headYaw,
                outPitch = data.playerInfo.headPitch;

        float yawDelta = MathUtils.getAngleDelta(data.playerInfo.from.yaw, outYaw),
                pitchDelta = MathUtils.getAngleDelta(data.playerInfo.from.pitch, outPitch);

        float yawDifference = Math.abs(lastYawDelta - yawDelta),
                pitchDifference = Math.abs(lastPitchDelta - pitchDelta);

        if (yawDifference > 2.0f && pitchDifference == 0.0f
                && yawDifference == lastYawDifference) {
            if ((verbose += 20) > 22) {
                vl++;
                flag("y=%1 vb=%2", yawDifference, verbose);
            }
        } else {
            verbose = Math.max(verbose - 1, 0);
        }

        lastYawDelta = yawDelta;
        lastPitchDelta = pitchDelta;
        lastYawDifference = yawDifference;
        lastPitchDifference = pitchDifference;

        debug("y=%1 p%2 vl=%3", yawDifference, pitchDifference, verbose);
    }
}
