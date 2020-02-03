package dev.brighten.anticheat.check.impl.movement.fly;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Fly (D)", description = "Checks if a player's acceleration is positive while in the air.",
        checkType = CheckType.FLIGHT, punishVL = 40)
@Cancellable
public class FlyD extends Check {
    
    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        if(packet.isPos()) {
            if(data.playerInfo.deltaY - data.playerInfo.lDeltaY > 0.01f
                    && data.playerInfo.airTicks > 2
                    && timeStamp - data.playerInfo.lastVelocityTimestamp > 150L
                    && !data.playerInfo.lClientGround
                    && data.playerInfo.liquidTimer.hasPassed(2)
                    && data.playerInfo.climbTimer.hasPassed(5)
                    && !data.playerInfo.flightCancel
                    && !data.playerInfo.clientGround
                    && !data.playerInfo.serverGround) {
                vl++;
                if(vl > 1 || !data.playerInfo.nearGround) {
                    flag("deltaY=%1 lDeltaY=%2 airTicks=%3",
                            data.playerInfo.deltaY, data.playerInfo.lDeltaY, data.playerInfo.airTicks);
                }
            } else vl-= vl > 0 ? 0.025f : 0;
        }
    }

}
