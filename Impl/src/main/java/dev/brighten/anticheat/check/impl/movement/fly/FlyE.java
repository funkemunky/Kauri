package dev.brighten.anticheat.check.impl.movement.fly;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.MovementUtils;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Fly (E)", description = "Checks for invalid jump heights.",
        checkType = CheckType.FLIGHT, punishVL = 2)
public class FlyE extends Check {

    @Packet
    public void onPacket(WrappedInFlyingPacket packet) {
        if(packet.isPos()) {
            float maxHeight = MovementUtils.getJumpHeight(data.getPlayer());
            if(!data.playerInfo.flightCancel
                    && data.playerInfo.jumped
                    && !data.playerInfo.wasOnSlime
                    && !data.blockInfo.onHalfBlock
                    && data.playerInfo.deltaY > maxHeight) {
                vl++;
                flag("deltaY=" + data.playerInfo.deltaY + " maxHeight=" + maxHeight);
            }
        }
    }
}
