package dev.brighten.anticheat.check.impl.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.TickTimer;
import cc.funkemunky.api.utils.objects.Interval;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.CheckType;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.MiscUtils;

@CheckInfo(name = "Aim (D)", description = "Designed to detect aimassists attempting that use cinematic smoothing.",
        checkType = CheckType.AIM, punishVL = 10)
public class AimD extends Check {

    private float lastYawAccel;
    private long lastDelta;
    private EvictingList<Float> gcdList = new EvictingList<>(50);

    @Packet
    public void onPacket(WrappedInFlyingPacket packet) {
        if(packet.isLook()) {
            float deltaYaw = data.playerInfo.deltaYaw;
            float accel = data.playerInfo.deltaYaw - data.playerInfo.lDeltaYaw;
            float deltaAccel = accel - lastYawAccel;
            float expand = (float)MiscUtils.EXPANDER;
            float gcd = MiscUtils.gcd((long)(data.playerInfo.deltaYaw * expand),
                    (long)(data.playerInfo.lDeltaYaw * expand)) / expand;
            if(gcd >= 0.01) {
                gcdList.add(gcd);
            } else {
                debug(Color.Red + "shit");
            }

            //Making sure to get shit within the std for a more accurate result.
            float mode = MathUtils.getMode(gcdList);

            long delta = getDelta(deltaYaw, mode);
            float sensitivity = getSensitivityFromGCD(mode);

            debug("sens=" + Color.Green + MathUtils.floor(sensitivity / .5f * 100)
                    + "%" + Color.Gray + " \u0394mouseX=" + delta);

            debug("\u0394yaw=" + deltaYaw + " accel=" + accel
                    + " \u0394accel=" + deltaAccel
                    + " gcd=" + gcd + " mode=" + mode);

            lastYawAccel = accel;
        }
    }
    //TODO Condense. This is just for easy reading until I test everything.
    private static int getDelta(float yawDelta, float gcd) {
        float f2 = yawToF2(yawDelta);
        float sens = getSensitivityFromGCD(gcd);
        float f = sens * .6f + .2f;
        float f1 = (float)Math.pow(f, 3) * 8;

        return MathUtils.floor(f2 / f1);
    }

    //TODO Condense. This is just for easy reading until I test everything.
    private static float getSensitivityFromGCD(float gcd) {
        float stepOne = yawToF2(gcd) / 8;
        float stepTwo = (float)Math.cbrt(stepOne);
        float stepThree = stepTwo - .2f;
        float stepFour = stepThree / .6f;
        return stepFour;
    }

    private static float yawToF2(float yawDelta) {
        return yawDelta / .15f;
    }
}
