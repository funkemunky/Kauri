package dev.brighten.anticheat.check.impl.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Aim (B)", description = "Checks for common denominators in pitch difference.",
        checkType = CheckType.AIM, punishVL = 80)
public class AimB extends Check {

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(packet.isLook()
                && Math.abs(data.playerInfo.deltaPitch) > 1E-5
                && Math.abs(data.playerInfo.to.pitch) < 78) {
            if(data.playerInfo.pitchGCD < 100000
                    && (MathUtils.getDelta(
                            MathUtils.getDelta(data.playerInfo.cinematicPitch, data.playerInfo.lCinematicPitch),
                    data.playerInfo.deltaYaw) > 1.2)
                    && !data.playerInfo.cinematicModePitch
                    && (data.playerInfo.deltaPitch < 10 || MathUtils.getDelta(data.playerInfo.deltaPitch, data.playerInfo.lDeltaPitch) > 10)
                    && data.playerInfo.lastAttack.hasNotPassed(20)) {
                if(vl++ > 20) {
                    flag("offset=%1 deltaPitch=%2", data.playerInfo.pitchGCD, data.playerInfo.deltaPitch);
                }
            } else vl-= vl > 0 ? 0.5 : 0;
            debug("gcd=" + data.playerInfo.pitchGCD
                    + " deltaPitch=" + data.playerInfo.deltaPitch + " vl=" + vl);
        }
    }
}
