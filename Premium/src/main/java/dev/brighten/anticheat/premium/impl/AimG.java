package dev.brighten.anticheat.premium.impl;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.processing.MovementProcessor;
import dev.brighten.anticheat.utils.MiscUtils;
import dev.brighten.anticheat.utils.Verbose;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Aim (G)", description = "Checks for bad GCD bypasses. (Rhys collab)",
        checkType = CheckType.AIM, punishVL = 30)
public class AimG extends Check {

    private Verbose verbose = new Verbose(40, 15);
    private float lastDeltaPitch;

    @Packet
    public void process(WrappedInFlyingPacket packet) {
        if(!packet.isLook()) return;

        float deltaPitch = Math.abs(modulo(Math.min(1, data.moveProcessor.sensitivityY), data.playerInfo.to.pitch)
                - data.playerInfo.to.pitch);

        long gcd = MiscUtils.gcd((long)(deltaPitch * MovementProcessor.offset),
                (long)(lastDeltaPitch * MovementProcessor.offset));

        if(deltaPitch < 9E-5f
                && gcd < 1000
                && data.moveProcessor.yawGcdList.size() > 40
                && MathUtils.getDelta(data.moveProcessor.sensXPercent, data.moveProcessor.sensYPercent) < 2) {
            if(verbose.flag(1, 12)) {
                vl++;
                flag("deltaPitch=%v", deltaPitch);
            }
        } else verbose.subtract(1);

        debug("deltaPitch=%v gcd=%v buffer=%v.1", deltaPitch, gcd, verbose.value());
        lastDeltaPitch = deltaPitch;
    }

    private static float modulo(float s, float angle) {
        float f = (s * 0.6f + .2f);
        float f2 = f * f * f * 1.2f;
        return angle - (angle % f2);
    }
}