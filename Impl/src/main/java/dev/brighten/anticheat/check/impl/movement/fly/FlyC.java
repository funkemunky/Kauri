package dev.brighten.anticheat.check.impl.movement.fly;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInKeepAlivePacket;
import cc.funkemunky.api.utils.TickTimer;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Fly (C)", description = "Checks if a player accelerates vertically before touching ground.",
        checkType = CheckType.FLIGHT, developer = true, punishVL = 10)
@Cancellable
public class FlyC extends Check {

    private int velocityTicks;
    @Packet
    public void onKeepAlive(WrappedInKeepAlivePacket packet) {
        if(packet.getTime() == data.getKeepAliveStamp("velocity")) {
           velocityTicks = 2;
        }
    }
    @Packet
    public void onPacket(WrappedInFlyingPacket packet) {
        if(packet.isPos()
                && velocityTicks-- <= 0
                && !data.playerInfo.nearGround
                && !data.playerInfo.clientGround
                && !data.playerInfo.lClientGround
                && data.playerInfo.airTicks > 2
                && !data.playerInfo.flightCancel) {
            if(data.playerInfo.deltaY > data.playerInfo.lDeltaY) {
                vl++;
                flag("%v.2>-%v.2", data.playerInfo.deltaY, data.playerInfo.lDeltaY);
            }
        }
    }
}
