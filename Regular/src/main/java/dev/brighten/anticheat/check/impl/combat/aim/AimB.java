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
                && Math.abs(data.playerInfo.deltaPitch) > 1E-5) {
            if(data.playerInfo.pitchGCD < 100000
                    && !data.playerInfo.cinematicMode
                    && data.playerInfo.lastTeleportTimer.isPassed(1)
                    && Math.abs(data.playerInfo.to.pitch) < 80) {
                if(++vl > 28) {
                    flag("offset=%s deltaPitch=%s", data.playerInfo.pitchGCD, data.playerInfo.deltaPitch);
                }
            } else vl-= vl > 0 ? 0.5 : 0;
            debug("gcd=%s cin=%s dpitch=%s ldp=%s pitch=%.2f lpitch=%.2f vl=%.1f",
                    data.playerInfo.pitchGCD,
                   data.playerInfo.cinematicMode, data.playerInfo.deltaPitch, data.playerInfo.lDeltaPitch,
                    data.playerInfo.to.pitch, data.playerInfo.from.pitch, vl);
        }
    }
}
