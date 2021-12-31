package dev.brighten.anticheat.check.impl.regular.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Aim (B)", description = "Checks for common denominators in pitch difference.",
        checkType = CheckType.AIM, punishVL = 45, executable = true)
public class AimB extends Check {

    private float buffer;
    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        float modeToUse = data.moveProcessor.sensitivityMcp;
        boolean goodGcd = Math.max(data.moveProcessor.yawMode, data.moveProcessor.pitchMode)
                % modeToUse < 0.001;
        if(packet.isLook()
                && Math.abs(data.playerInfo.deltaPitch) > 1E-5) {
            if(data.playerInfo.pitchGCD < 0.007
                    && MathUtils.getDelta(data.moveProcessor.sensXPercent, data.moveProcessor.sensYPercent) > 1
                    && !goodGcd
                    && data.moveProcessor.lastCinematic.isPassed(2)
                    && data.playerInfo.lastTeleportTimer.isPassed(1)
                    && Math.abs(data.playerInfo.to.pitch) < 80) {
                if(++buffer > 14) {
                    buffer = 14;
                    vl++;
                    flag("offset=%s deltaPitch=%s", data.playerInfo.pitchGCD, data.playerInfo.deltaPitch);
                }
            } else buffer-= buffer > 0 ? 0.5f : 0;
            debug("gcd=%s cin=%s dpitch=%s ldp=%s pitch=%.2f lpitch=%.2f vl=%.1f",
                    data.playerInfo.pitchGCD,
                   data.playerInfo.cinematicMode, data.playerInfo.deltaPitch, data.playerInfo.lDeltaPitch,
                    data.playerInfo.to.pitch, data.playerInfo.from.pitch, buffer);
        }
    }
}
