package dev.brighten.anticheat.check.impl.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.CheckType;
import dev.brighten.anticheat.check.api.Packet;

@CheckInfo(name = "Aim (H)", description = "Checks for rounded pitch deltas.",
        checkType = CheckType.AIM, punishVL = 12)
public class AimH extends Check {

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(packet.isLook()) {
            float absPitch = Math.abs((float) MathUtils.trim(4, data.playerInfo.deltaPitch));
            String string = String.valueOf(absPitch);
            String string2 = String.valueOf((float) MathUtils.trim(4, data.playerInfo.deltaYaw));
            if((absPitch > 1 && string.length() < 4) && data.playerInfo.lastAttack.hasNotPassed(10)) {
                if(vl++ > 4) {
                    flag("deltaPitch=" + absPitch);
                }
            } else vl-= vl > 0 ? 0.5 : 0;

            debug("deltaPitch=" + absPitch + " vl=" + vl + " length=" + string.length());
        }
    }
}
