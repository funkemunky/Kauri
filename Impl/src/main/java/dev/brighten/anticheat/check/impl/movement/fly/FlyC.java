package dev.brighten.anticheat.check.impl.movement.fly;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInKeepAlivePacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Fly (C)", description = "Checks if a player accelerates vertically before touching ground.",
        checkType = CheckType.FLIGHT, punishVL = 10)
@Cancellable
public class FlyC extends Check {

    @Packet
    public void onPacket(WrappedInFlyingPacket packet) {
        if(packet.isPos()
                && data.playerInfo.lastVelocity.hasPassed(4)
                && !data.playerInfo.nearGround
                && !data.playerInfo.clientGround
                && !data.playerInfo.lClientGround
                && data.playerInfo.airTicks > 5
                && !data.playerInfo.flightCancel) {
            if(data.playerInfo.deltaY > data.playerInfo.lDeltaY) {
                vl++;
                flag("%v.2>-%v.2 at=%v",
                        data.playerInfo.deltaY, data.playerInfo.lDeltaY, data.playerInfo.airTicks);
            }
        }
    }
}
