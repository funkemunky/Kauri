package dev.brighten.anticheat.check.impl.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.processing.MovementProcessor;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Aim (L)", description = "A general aim check.", checkType = CheckType.AIM,
        developer = true, punishVL = 500)
public class AimL extends Check {

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(packet.isLook()) {
            debug("gcd=%1 sens=%2 deltamY=%3", data.playerInfo.pitchGCD,
                    MovementProcessor.sensToPercent(data.moveProcessor.sensitivityX), data.moveProcessor.deltaY);
        }
    }
}
