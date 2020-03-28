package dev.brighten.anticheat.premium.impl;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockPlacePacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.Color;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Aim (J)", description = "Checks the gcd of yaw delta.", checkType = CheckType.AIM, developer = true)
public class AimJ extends Check {

    @Packet
    public void onLook(WrappedInFlyingPacket packet) {
        if(packet.isLook()) {
            if(data.playerInfo.yawGCD < 1E6 && data.playerInfo.deltaYaw < 8) {
                debug(Color.Green + "Flag");
            }
            debug("gcd=%v deltaYaw=%v.2 sens=%v deltaX=%v", data.playerInfo.yawGCD,
                    data.playerInfo.deltaYaw, data.moveProcessor.sensXPercent, data.moveProcessor.deltaX);
        }
    }
}
