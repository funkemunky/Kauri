package dev.brighten.anticheat.check.impl.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.CheckType;
import dev.brighten.anticheat.check.api.Packet;

@CheckInfo(name = "Aim (I)", description = "Checks pitch accel", checkType = CheckType.AIM)
public class AimI extends Check {

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(packet.isLook()) {
            if(MathUtils.getDelta(data.playerInfo.deltaPitch, data.playerInfo.lDeltaPitch) < 1E-5
                    && (data.playerInfo.deltaPitch > 0 || data.playerInfo.deltaYaw > 0.8)
                    && (data.playerInfo.deltaXZ > 0
                    || data.playerInfo.deltaY > 0
                    || data.playerInfo.lastAttack.hasNotPassed(20))
                    && Math.abs(data.playerInfo.to.pitch) < 70) {
                if(data.playerInfo.deltaYaw > 0.4 && vl++ > 10) {
                    flag("shit=" + data.playerInfo.deltaPitch);
                }
            } else vl-= vl > 0 ? 2 : 0;

            debug("cin=" + data.playerInfo.cinematicPitch + " pitch=" + data.playerInfo.to.pitch);
        }
    }

}
