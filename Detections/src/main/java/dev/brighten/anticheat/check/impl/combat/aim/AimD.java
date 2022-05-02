package dev.brighten.anticheat.check.impl.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.math.cond.MaxDouble;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Aim (D)", description = "Checks for the rounding of pitch.", checkType = CheckType.AIM
        , punishVL = 12, executable = true)
public class AimD extends Check {

    private MaxDouble verbose = new MaxDouble(100);
    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(packet.isLook() && Math.abs(data.playerInfo.deltaPitch) >= 0.5f) {

            float shit1 = data.playerInfo.deltaPitch % 0.1f, shit2 = data.playerInfo.deltaPitch % 0.05f;
            if(data.playerInfo.deltaPitch > 0 && Math.abs(data.playerInfo.deltaPitch) < 100
                    && (shit1 == 0 || shit2 == 0 || data.playerInfo.deltaPitch % 1f == 0)) {
                if(verbose.add(1) > 10) {
                    vl++;
                    flag("deltaPitch=%s trimmed=%s vb=%s", data.playerInfo.deltaPitch,
                            data.playerInfo.deltaPitch, verbose.value());
                }
            } else verbose.subtract(0.25);

            debug("trimmed=" + data.playerInfo.deltaPitch + " dp=" + data.playerInfo.deltaPitch + " 1=" + shit1 + " 2=" + shit2
                    + " verbose=" + verbose);
        }
    }
}
