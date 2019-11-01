package dev.brighten.anticheat.check.impl.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.CheckType;
import dev.brighten.anticheat.check.api.Packet;

@CheckInfo(name = "Aim (G)", description = "Another style of denominator check on both pitch and yaw.",
        checkType = CheckType.AIM, punishVL = 80, enabled = false, executable = false)
public class AimG extends Check {

    private long lastGCD;
    @Packet
    public void onPacket(WrappedInFlyingPacket packet) {
        if(packet.isLook()) {
            float yawAngle = (data.playerInfo.to.yaw - data.playerInfo.from.yaw) / .15f;

            debug("to=" + data.playerInfo.to.yaw + " from=" + data.playerInfo.from.yaw);
        }
    }
}
