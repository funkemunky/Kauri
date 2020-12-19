package dev.brighten.anticheat.premium.impl;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.MiscUtils;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Aim (J)",  description = "just testing some random shit", checkType = CheckType.AIM)
public class AimJ extends Check {

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(packet.isLook()) {
            float deltaYaw = MiscUtils.getDistanceBetweenAngles(data.playerInfo.to.yaw, data.playerInfo.from.yaw);
            float deltaYaw2 = MiscUtils.distanceBetweenAngles(data.playerInfo.to.yaw, data.playerInfo.from.yaw);

            debug("1=%v 2=%v", deltaYaw, deltaYaw2);
        }
    }
}
