package dev.brighten.anticheat.check.impl.regular.movement.fly;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;

@CheckInfo(name = "Fly (D)", description = "Air modification check", checkType = CheckType.FLIGHT,
        devStage = DevStage.BETA)
@Cancellable(cancelType = CancelType.MOVEMENT)
public class FlyD extends Check {

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(!packet.isPos()
                || data.playerInfo.flightCancel
                || (data.playerInfo.nearGroundTimer.isNotPassed(3) && (data.playerInfo.lClientGround
                || data.playerInfo.clientGround))
                || data.playerInfo.lastBlockPlace.isNotPassed(3)
                || data.playerInfo.lastVelocity.isNotPassed(3)
        ) return;

        if(data.playerInfo.deltaY > data.playerInfo.lDeltaY) {
            vl++;
            flag("dy=%.2f ldy=%.2f", data.playerInfo.deltaY, data.playerInfo.lDeltaY);

            fixMovementBugs();
        }
    }
}
