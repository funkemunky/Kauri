package dev.brighten.anticheat.premium.impl;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Aim (H)", description = "checks for large headsnaps.",
        developer = true, checkType = CheckType.AIM, vlToFlag = 3)
public class AimH extends Check {

    private float lDeltaYaw;
    @Packet
    public void process(final WrappedInFlyingPacket packet) {
        if(packet.isLook()) {
            double deltaYaw = Math.abs(data.playerInfo.to.yaw - data.playerInfo.from.yaw);
            double delta = Math.abs(deltaYaw - lDeltaYaw);

            if(delta > 80 && lDeltaYaw < 100 && deltaYaw > 320) {
                vl++;
                flag("dyaw=%v.1 ldyaw=%v.1", deltaYaw, lDeltaYaw);
            } else if(vl > 0) vl-= 0.01;
        } else lDeltaYaw = 0;
    }
}