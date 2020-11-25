package dev.brighten.anticheat.check.impl.movement.fly;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Fly (D)", description = "Air modification check", checkType = CheckType.FLIGHT, developer = true)
@Cancellable(cancelType = CancelType.MOVEMENT)
public class FlyD extends Check {

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(!packet.isPos()
                || data.playerInfo.flightCancel
                || data.playerInfo.nearGround
                || data.playerInfo.clientGround
                || data.playerInfo.lClientGround
                || data.playerInfo.airTicks <= 2
                || data.playerInfo.lastVelocity.isNotPassed(3)
        ) return;

        if(data.playerInfo.deltaY > data.playerInfo.lDeltaY) {
            vl++;
            flag("dy=%v.2 ldy=%v.2", data.playerInfo.deltaY, data.playerInfo.lDeltaY);
        }
    }
}
