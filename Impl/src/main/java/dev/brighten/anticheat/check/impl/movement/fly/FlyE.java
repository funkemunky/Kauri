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
import lombok.val;

@CheckInfo(name = "Fly (E)", description = "Checks for invalid jump heights.",
        checkType = CheckType.FLIGHT, punishVL = 5, vlToFlag = 2)
@Cancellable
public class FlyE extends Check {

    private MaxDouble verbose = new MaxDouble(5);
    @Packet
    public void onPacket(WrappedInFlyingPacket packet) {
        if(packet.isPos()) {
            float maxHeight = MovementUtils.getJumpHeight(data.getPlayer());
            val shit = (data.playerInfo.lastVelocity.hasNotPassed(2)
                    ? data.playerInfo.velocityY : maxHeight);
            if(!data.playerInfo.flightCancel
                    && data.playerInfo.jumped
                    && !data.playerInfo.wasOnSlime
                    && !data.blockInfo.blocksAbove
                    && data.playerInfo.lastBlockPlace.hasPassed(10)
                    && data.playerInfo.halfBlockTicks.value() == 0
                    && MathUtils.getDelta(data.playerInfo.deltaY, shit) > 0.01f
                    && MathUtils.getDelta(data.playerInfo.deltaY, maxHeight) > 0.01f) {
                if(verbose.add() > 2 || data.playerInfo.deltaY > shit) {
                    vl++;
                    flag("deltaY=%1 maxHeight=%2 vel=%3", data.playerInfo.deltaY, maxHeight, shit);
                }
            } else verbose.subtract(0.05);

            debug("deltaY=" + data.playerInfo.deltaY + " half=" + data.playerInfo.halfBlockTicks);
        }
        vl-= vl > 0 ? 0.002f : 0;
    }
}
