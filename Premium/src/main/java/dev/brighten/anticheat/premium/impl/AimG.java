package dev.brighten.anticheat.premium.impl;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.processing.MovementProcessor;
import dev.brighten.anticheat.utils.MiscUtils;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Aim (G)", description = "Checks for bad GCD bypasses. (Rhys collab)",
        checkType = CheckType.AIM, punishVL = 30)
public class AimG extends Check {

    private float lastDeltaPitch, lastDeltaYaw;

    @Packet
    public void process(WrappedInFlyingPacket packet, long now) {
        if(!packet.isLook()) return;

        float deltaPitch = Math.abs(modulo(Math.min(1, data.moveProcessor.sensitivityY), data.playerInfo.to.pitch)
                - data.playerInfo.to.pitch);
        float deltaYaw = Math.abs(modulo(Math.min(1, data.moveProcessor.sensitivityX), data.playerInfo.to.yaw)
                - data.playerInfo.to.yaw);

        long gcd = MiscUtils.gcd((long)(deltaPitch * MovementProcessor.offset),
                (long)(lastDeltaPitch * MovementProcessor.offset)),
                gcdYaw = MiscUtils.gcd((long)(deltaYaw * MovementProcessor.offset),
                        (long)(lastDeltaYaw * MovementProcessor.offset));

        if(gcd < 200L
                && data.moveProcessor.yawGcdList.size() > 40
                && MathUtils.getDelta(data.moveProcessor.sensXPercent, data.moveProcessor.sensYPercent) < 2) {
            if(++vl > 3) {
                flag("gcd=%v", gcd);
            }
        } else if(vl > 0) vl-= 0.2f;

        debug("gcd=%v gcdYaw=%v vl=%v.1", gcd, gcdYaw, vl);

        lastDeltaYaw = deltaYaw;
        lastDeltaPitch = deltaPitch;
    }

    private static float modulo(float s, float angle) {
        float f = (s * 0.6f + .2f);
        float f2 = f * f * f * 1.2f;
        return angle - (angle % f2);
    }
}