package dev.brighten.anticheat.premium.impl;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.Color;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Aim (I)", description = "Checks for bad gcd patches.", checkType = CheckType.AIM,
        developer = true)
public class AimI extends Check {

    private float ldelta;
    private int buffer;

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(!packet.isLook()) return;

        float clampedPitch = modulo(data.moveProcessor.sensitivityY, data.playerInfo.to.pitch),
                clampedYaw = modulo(data.moveProcessor.sensitivityX, data.playerInfo.to.yaw);

        float deltaPitch = Math.abs(clampedPitch - data.playerInfo.to.pitch),
                deltaYaw = Math.abs(clampedYaw - data.playerInfo.to.yaw);
        float sub = Math.abs(deltaPitch - ldelta);

        if(deltaPitch < 0.001 && deltaYaw < 0.02 && sub > 0 && deltaPitch > 1E-10) {
            buffer++;

            if(buffer > 20) {
                vl++;
                flag("cpitchDif=%v cyawDif=%v.3 deltaClamp=%v buffer=%v",
                        deltaPitch, deltaYaw, sub, buffer);
            }
            debug(Color.Green + "Flag");
        } else if(buffer > 0) buffer--;

        debug("p=%v.5 clamped=%v.5 deltaPitch=%v.5 deltaYaw=%v.5 sub=%v buffer=%v sens=%v",
                packet.getPitch(), clampedPitch, deltaPitch, deltaYaw, sub, buffer, data.moveProcessor.sensitivityX);
        ldelta = deltaPitch;
    }

    private static float modulo(float s, float angle) {
        float f = (s * 0.6f + .2f);
        float f2 = f * f * f * 1.2f;
        return angle - (angle % f2);
    }
}
