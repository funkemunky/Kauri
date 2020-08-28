package dev.brighten.anticheat.premium.impl;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Speed (D)", description = "Minecraft code acceleration check.",
        checkType = CheckType.SPEED, developer = true)
public class SpeedD extends Check {

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(!packet.isPos()
                || (data.playerInfo.deltaY == 0 || data.playerInfo.deltaXZ == 0)
                || data.playerInfo.serverPos) return;
        
        
    }
}
