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
                    && data.moveProcessor.sensitivityX < 0.44) {
                if(++vl > 28) {
                    flag("offset=%v deltaPitch=%v", data.playerInfo.pitchGCD, data.playerInfo.deltaPitch);
                }
            } else vl-= vl > 0 ? 0.5 : 0;
            //debug("gcd=%v cin=%v dpitch=%v ldp=%v pitch=%v.2 lpitch=%v.2 vl=%v.1",
            //        data.playerInfo.pitchGCD,
            //       data.playerInfo.cinematicMode, data.playerInfo.deltaPitch, data.playerInfo.lDeltaPitch,
            //        data.playerInfo.to.pitch, data.playerInfo.from.pitch, vl);

            debug("(%v) yaw=%v.4 pitch=%v.4 syaw=%v.4 spitch=%v.4",
                    data.playerInfo.cinematicMode, data.playerInfo.to.yaw, data.playerInfo.to.pitch,
                    data.moveProcessor.smoothYaw, data.moveProcessor.smoothPitch);
        }
    }
}
