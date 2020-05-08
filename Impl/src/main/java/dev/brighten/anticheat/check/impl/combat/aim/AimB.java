package dev.brighten.anticheat.check.impl.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Aim (B)", description = "Checks for common denominators in pitch difference.",
        checkType = CheckType.AIM, punishVL = 45)
public class AimB extends Check {

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(packet.isLook()
                && Math.abs(data.playerInfo.deltaPitch) > 1E-5
                && Math.abs(data.playerInfo.deltaPitch) < 20
                && Math.abs(data.playerInfo.to.pitch) < 78) {
            if(data.playerInfo.pitchGCD < 100000
                    && data.playerInfo.lastAttack.hasNotPassed(10)
                    && !data.playerInfo.cinematicMode
                    && data.moveProcessor.sensitivityX < 0.44) {
                if(++vl > 28) {
                    flag("offset=%v deltaPitch=%v", data.playerInfo.pitchGCD, data.playerInfo.deltaPitch);
                }
            } else vl-= vl > 0 ? 0.5 : 0;
            debug("gcd=%v cin=%v dpitch=%v.2 vl=%v.1",
                    data.playerInfo.pitchGCD,
                    data.playerInfo.cinematicMode, data.playerInfo.deltaPitch, vl);
        }
    }
}
