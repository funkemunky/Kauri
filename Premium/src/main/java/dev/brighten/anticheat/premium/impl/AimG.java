package dev.brighten.anticheat.premium.impl;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.Color;
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

    @Packet
    public void process(WrappedInFlyingPacket packet) {
        if(!packet.isLook()) return;

        float sens = MovementProcessor.percentToSens(data.moveProcessor.sensYPercent);
        float deltaPitch = Math.abs(modulo(sens, data.playerInfo.to.pitch)
                - data.playerInfo.to.pitch);
        float clampedYaw = MathUtils.yawTo180F(data.playerInfo.to.yaw);
        float deltaYaw = Math.abs(modulo(sens, clampedYaw) - clampedYaw);

        if(deltaYaw < 0.01 && deltaPitch < 8E-5) {
            debug(Color.Green + "Flag");
        }

        debug("deltaPitch=%v deltaYaw=%v sens=%v buffer=%v.1", deltaPitch, deltaYaw,
                data.moveProcessor.sensYPercent + ", " + data.moveProcessor.sensXPercent, verbose.value());
    }

    private static float modulo(float s, float angle) {
        float f = (s * 0.6f + .2f);
        float f2 = f * f * f * 1.2f;
        return angle - (angle % f2);
    }
}