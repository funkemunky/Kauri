package dev.brighten.anticheat.premium.impl;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Aim (I)", description = "Checks for bad modulo gcd patches.", checkType = CheckType.AIM)
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

        if(deltaPitch < 5E-5 && sub > 0) {
            buffer+= 2;

            if(buffer > 20) {
                vl++;
                flag("clampedDif=%v.3 deltaClamp=%v.3 buffer=%v", deltaPitch, sub, buffer);
            }
            debug(Color.Green + "Flag");
        } else if(buffer > 0) buffer--;

        debug("p=%v.5 clamped=%v.5 deltaPitch=%v.5 deltaYaw=%v.5 sub=%v buffer=%v sens=%v", packet.getPitch(), clampedPitch,
                deltaPitch, deltaYaw, sub, buffer, data.moveProcessor.sensitivityX);
        ldelta = deltaPitch;
    }

    private static float modulo(float s, float angle) {
        float f = (s * 0.6f + .2f);
        float f2 = f * f * f * 1.2f;
        return angle - (angle % f2);
    }
}
