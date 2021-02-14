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

    private double groundY = 1 / 64.;
    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(!packet.isPos()
                || data.playerInfo.flightCancel
                || data.playerInfo.serverPos
                || data.playerInfo.nearGround
                || data.playerInfo.lClientGround
                || data.playerInfo.clientGround
                || data.playerInfo.lastTeleportTimer.isNotPassed(2 + data.lagInfo.transPing)
                || data.playerInfo.lastVelocity.isNotPassed(3)
        ) return;

        if(data.playerInfo.deltaY > data.playerInfo.lDeltaY) {
            vl++;
            flag("dy=%.2f ldy=%.2f", data.playerInfo.deltaY, data.playerInfo.lDeltaY);
        }
    }
}
