package dev.brighten.anticheat.check.impl.movement.fly;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.math.cond.MaxDouble;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.MovementUtils;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Fly (E)", description = "Checks for invalid jump heights.",
        checkType = CheckType.FLIGHT, punishVL = 5, vlToFlag = 2)
@Cancellable
public class FlyE extends Check {

    private MaxDouble verbose = new MaxDouble(5);
    @Packet
    public void onPacket(WrappedInFlyingPacket packet) {
        if(packet.isPos()) {
            float maxHeight = MovementUtils.getJumpHeight(data.getPlayer());
            if(!data.playerInfo.flightCancel
                    && data.playerInfo.jumped
                    && !data.playerInfo.wasOnSlime
                    && data.playerInfo.lClientGround
                    && !data.playerInfo.serverGround
                    && data.playerInfo.blockAboveTimer.hasPassed(6)
                    && data.playerInfo.lastBlockPlace.hasPassed(20)
                    && data.playerInfo.lastHalfBlock.hasPassed(4)
                    && data.playerInfo.lastVelocity.hasPassed(4)
                    && MathUtils.getDelta(data.playerInfo.deltaY, maxHeight) > 0.01f) {
                if(verbose.add() > 2) {
                    vl++;
                    flag("deltaY=%1 maxHeight=%2", data.playerInfo.deltaY, maxHeight);
                }
            } else verbose.subtract(0.05);

            debug("deltaY=%1 above=%2", data.playerInfo.deltaY,
                    data.playerInfo.blockAboveTimer.getPassed());
        }
        vl-= vl > 0 ? 0.002f : 0;
    }
}
