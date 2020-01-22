package dev.brighten.anticheat.check.impl.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.math.cond.MaxDouble;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Aim (G)", description = "Checks for the rounding of pitch.", checkType = CheckType.AIM, punishVL = 50)
public class AimG extends Check {

    private MaxDouble verbose = new MaxDouble(100);
    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(packet.isLook() && Math.abs(data.playerInfo.deltaPitch) >= 0.5f) {
            float trimmed = MathUtils.trimFloat(4, Math.abs(data.playerInfo.deltaPitch));

            float shit1 = trimmed % 0.1f, shit2 = trimmed % 0.05f;
            if(trimmed > 0 && Math.abs(data.playerInfo.deltaPitch) < 100
                    && (shit1 == 0 || shit2 == 0 || trimmed % 1f == 0)) {
                if(verbose.add(1) > 10) {
                    vl++;
                    flag("deltaPitch=%1 trimmed=%2 vb=%3", data.playerInfo.deltaPitch,
                            trimmed, verbose.value());
                }
            } else verbose.subtract(0.25);

            debug("trimmed=" + trimmed + " dp=" + data.playerInfo.deltaPitch + " 1=" + shit1 + " 2=" + shit2
                    + " verbose=" + verbose);
        }
    }
}
