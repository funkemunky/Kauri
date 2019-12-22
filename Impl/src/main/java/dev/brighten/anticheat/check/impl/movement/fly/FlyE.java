package dev.brighten.anticheat.check.impl.movement.fly;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.MovementUtils;
import dev.brighten.api.check.CheckType;
import lombok.val;

@CheckInfo(name = "Fly (E)", description = "Checks for invalid jump heights.",
        checkType = CheckType.FLIGHT, punishVL = 2)
public class FlyE extends Check {

    @Packet
    public void onPacket(WrappedInFlyingPacket packet) {
        if(packet.isPos()) {
            float maxHeight = MovementUtils.getJumpHeight(data.getPlayer());
            val shit = (data.playerInfo.lastVelocity.hasNotPassed(2)
                    ? data.playerInfo.velocityY : maxHeight);
            if(!data.playerInfo.flightCancel
                    && data.playerInfo.jumped
                    && !data.playerInfo.wasOnSlime
                    && !data.blockInfo.onHalfBlock
                    && MathUtils.getDelta(data.playerInfo.deltaY, shit) > 0.01f) {
                if(vl++ > 2 || data.playerInfo.deltaY > shit)
                    flag("deltaY=" + data.playerInfo.deltaY + " maxHeight=" + maxHeight);
            } else vl-= vl > 0 ? 0.05f : 0;
        }
    }
}
