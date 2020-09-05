package dev.brighten.anticheat.premium.impl;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MathUtils;
import com.google.common.collect.Lists;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.processing.MovementProcessor;
import dev.brighten.anticheat.utils.MiscUtils;
import dev.brighten.api.check.CheckType;
import lombok.val;

import java.util.Deque;

@CheckInfo(name = "Aim (G)", description = "Checks for bad GCD bypasses. (Rhys collab)",
        checkType = CheckType.AIM, punishVL = 30)
public class AimG extends Check {

    private float lastDeltaPitch;

    @Packet
    public void process(WrappedInFlyingPacket packet, long now) {
        if(!packet.isLook()) return;

        float deltaPitch = Math.abs(modulo(data.moveProcessor.sensitivityY, data.playerInfo.to.pitch)
                - data.playerInfo.to.pitch);

        long gcd = MiscUtils.gcd((long)(deltaPitch * MovementProcessor.offset),
                (long)(lastDeltaPitch * MovementProcessor.offset));

        if(gcd < 500L
                && data.moveProcessor.yawGcdList.size() > 40
                && MathUtils.getDelta(data.moveProcessor.sensXPercent, data.moveProcessor.sensYPercent) < 2) {
            if(++vl > 3) {
                flag("gcd=%v", gcd);
            }
        } else if(vl > 0) vl-= 0.2f;

        debug("gcd=%v vl=%v.1", gcd, vl);

        lastDeltaPitch = deltaPitch;
    }

    private static float modulo(float s, float angle) {
        float f = (s * 0.6f + .2f);
        float f2 = f * f * f * 1.2f;
        return angle - (angle % f2);
    }
}