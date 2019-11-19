package dev.brighten.anticheat.check.impl.movement.fly;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.CheckType;
import dev.brighten.anticheat.check.api.Packet;

@CheckInfo(name = "Fly (D)", description = "Checks for legitimate jump heights.",
        checkType = CheckType.FLIGHT, punishVL = 15)
public class FlyD extends Check {

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        if(packet.isPos()) {
            if(data.playerInfo.jumped
                    && !data.playerInfo.collidesVertically
                    && data.playerInfo.halfBlockTicks == 0
                    && !data.playerInfo.flightCancel
                    && !data.playerInfo.clientGround
                    && !data.lagInfo.lagging
                    && !data.playerInfo.wasOnSlime
                    && data.playerInfo.lastBlockPlace.hasPassed(10)
                    && timeStamp - data.playerInfo.lastVelocityTimestamp > 250L) {
                if(!MathUtils.approxEquals(0.001, data.playerInfo.jumpHeight, data.playerInfo.deltaY)) {
                    if(vl++ > 2) {
                        flag("deltaY=" + data.playerInfo.deltaY + " predicted=" + data.playerInfo.jumpHeight);
                    }
                } else vl-= vl > 0 ? 0.5 : 0;
                debug("deltaY=" + data.playerInfo.deltaY + " predicted=" + data.playerInfo.jumpHeight + " vl=" + vl);
            }
        }
    }

}
