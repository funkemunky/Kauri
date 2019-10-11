package dev.brighten.anticheat.check.impl.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
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
            float absPitch = Math.abs(data.playerInfo.deltaPitch);
            if(absPitch > 1 && absPitch % 0.1 == 0) {
                if(vl++ > 4) {
                    flag("deltaPitch=" + absPitch);
                }
            } else vl-= vl > 0 ? 0.1 : 0;

            debug("deltaPitch=" + absPitch + " vl=" + vl);
        }
    }
}
