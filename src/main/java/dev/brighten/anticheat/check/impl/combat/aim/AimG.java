package dev.brighten.anticheat.check.impl.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.CheckType;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.processing.MovementProcessor;
import dev.brighten.anticheat.utils.MiscUtils;

@CheckInfo(name = "Aim (G)", description = "Another style of denominator check on both pitch and yaw.",
        checkType = CheckType.AIM, punishVL = 80, enabled = false, executable = false)
public class AimG extends Check {

    private long lastGCD;
    @Packet
    public void onPacket(WrappedInFlyingPacket packet) {
        if(packet.isLook()) {
            long gcd = MiscUtils.gcd((long)(data.playerInfo.deltaYaw * MovementProcessor.offset),
                    (long)(data.playerInfo.deltaPitch * MovementProcessor.offset));

            if(gcd < 1E5 && (Math.abs(data.playerInfo.deltaPitch) > 0.5 || data.playerInfo.deltaYaw > 0.5)
                    && data.playerInfo.deltaYaw < 12
                    && data.playerInfo.deltaPitch < 12
                    && !data.playerInfo.cinematicModeYaw
                    && !data.playerInfo.cinematicModePitch) {
                if(vl++ > 40) {
                    flag("g=" + gcd + " yawDelta=" + data.playerInfo.deltaYaw + " pitchDelta=" + data.playerInfo.deltaPitch);
                }
            } else vl-= vl > 0 ? 0.25 : 0;

            debug("g=" + gcd);

            lastGCD = gcd;
        }
    }
}
