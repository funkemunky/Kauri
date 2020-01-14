package dev.brighten.anticheat.check.impl.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.processing.MovementProcessor;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Aim (C)", description = "Checks for common denominators in yaw difference.",
        checkType = CheckType.AIM, punishVL = 100)
public class AimC extends Check {

    private double lastGcd;
    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(!packet.isLook()) return;

        double gcd = (data.playerInfo.yawGCD / MovementProcessor.offset);

        if(MathUtils.getDelta(gcd, lastGcd) > 1E-4) {
            vl++;
        } else vl = 0;

        debug(gcd + ", " + vl);

        lastGcd = gcd;
    }
}
