package dev.brighten.anticheat.check.impl.movement.fly;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.MovementUtils;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Fly (C)", description = "Checks for invalid jump heights.",
        checkType = CheckType.FLIGHT, punishVL = 4, vlToFlag = 2, executable = true)
@Cancellable
public class FlyC extends Check {

    @Packet
    public void onPacket(WrappedInFlyingPacket packet) {
        if (packet.isPos()) {
            if (!data.playerInfo.flightCancel
                    && data.playerInfo.jumped
                    && !data.playerInfo.wasOnSlime
                    && !data.blockInfo.collidesHorizontally
                    && data.playerInfo.lClientGround
                    && !data.blockInfo.miscNear
                    && data.playerInfo.lastGhostCollision.isNotPassed(1)
                    && !data.playerInfo.insideBlock
                    && data.playerInfo.blockAboveTimer.isPassed(6)
                    && data.playerInfo.lastBlockPlace.isPassed(20)
                    && data.playerInfo.lastHalfBlock.isPassed(4)
                    && data.playerInfo.lastVelocity.isPassed(4)
                    && MathUtils.getDelta(data.playerInfo.deltaY, data.playerInfo.jumpHeight) > 0.01f) {
                vl++;
                flag("deltaY=%.4f maxHeight=%.4f", data.playerInfo.deltaY, data.playerInfo.jumpHeight);

                fixMovementBugs();
            } else if(vl > 0) vl-= 0.01f;

            debug("deltaY=%s above=%s", data.playerInfo.deltaY,
                    data.playerInfo.blockAboveTimer.getPassed());
        }
    }
}
