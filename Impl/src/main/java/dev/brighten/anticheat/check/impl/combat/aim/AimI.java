package dev.brighten.anticheat.check.impl.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.processing.MovementProcessor;
import dev.brighten.api.check.CheckType;
import lombok.val;

@CheckInfo(name = "Aim (I)", description = "Checks for gcd on yaw.", checkType = CheckType.AIM,
        developer = true, punishVL = 20)
public class AimI extends Check {

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(packet.isLook()) {
            val sens = MovementProcessor.sensToPercent(data.moveProcessor.sensitivityX);
            debug("yaw=%1 smooth=%2", data.playerInfo.yawGCD, sens);
        }
    }
}
