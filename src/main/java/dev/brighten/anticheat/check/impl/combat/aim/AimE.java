package dev.brighten.anticheat.check.impl.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.CheckType;
import dev.brighten.anticheat.check.api.Packet;

@CheckInfo(name = "Aim (E)", description = "Checks for very large differences in deviation of yaw.",
        checkType = CheckType.AIM)
public class AimE extends Check {

    @Packet
    public void flying(WrappedInFlyingPacket packet) {
        if(packet.isLook() && data.playerInfo.lastAttack.hasNotPassed(20)) {
            if(data.playerInfo.yawGCD > 9.9E7) {
                if(vl++ > 10) {
                    punish();
                } else if(vl > 4) flag("g=" + data.playerInfo.yawGCD);
            } else vl-= vl > 0 ? 0.5 : 0;
            debug("yawGCD=" + data.playerInfo.yawGCD);
        }
    }
}
