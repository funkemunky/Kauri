package dev.brighten.anticheat.check.impl.movement.fly;

import cc.funkemunky.api.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;

@CheckInfo(name = "Fly (D)", description = "Air modification check", checkType = CheckType.FLIGHT,
        devStage = DevStage.BETA, executable = true, punishVL = 5, vlToFlag = 2)
@Cancellable(cancelType = CancelType.MOVEMENT)
public class FlyD extends Check {

    @Packet
    public void onFlying(WrapperPlayClientPlayerFlying packet) {
        if (!packet.hasPositionChanged()
                || data.playerInfo.flightCancel
                || data.playerInfo.canUseElytra
                || data.playerInfo.doingBlockUpdate
                || (data.playerInfo.nearGroundTimer.isNotPassed(3) && (data.playerInfo.lClientGround
                || data.playerInfo.clientGround))
                || data.playerInfo.lastBlockPlace.isNotPassed(1)
                || data.playerInfo.lastVelocity.isNotPassed(8)
        )
            return;

        if(data.playerInfo.deltaY > data.playerInfo.lDeltaY) {
            vl++;
            flag("dy=%.2f ldy=%.2f", data.playerInfo.deltaY, data.playerInfo.lDeltaY);

            fixMovementBugs();
        }
    }
}
