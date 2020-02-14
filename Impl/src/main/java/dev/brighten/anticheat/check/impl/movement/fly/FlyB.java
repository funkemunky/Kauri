package dev.brighten.anticheat.check.impl.movement.fly;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.MovementUtils;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Fly (B)", description = "Checks for improper acceleration.", checkType = CheckType.FLIGHT)
@Cancellable
public class FlyB extends Check {

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        if(packet.isPos() && (data.playerInfo.deltaY != 0 || data.playerInfo.deltaXZ != 0)) {
            //We check if the player is in ground, since theoretically the y should be zero.
            double predicted = data.playerInfo.clientGround
                    ? 0 : (data.playerInfo.lDeltaY - 0.08) * 0.9800000190734863D;

            if(data.playerInfo.lClientGround && !data.playerInfo.clientGround) {
                predicted = Math.min(data.playerInfo.deltaY, MovementUtils.getJumpHeight(packet.getPlayer()));
            }

            if(data.playerVersion.isOrBelow(ProtocolVersion.V1_8_9) && Math.abs(predicted) < 0.005) {
                predicted = 0;
            }

            if(!data.playerInfo.flightCancel
                    && data.playerInfo.slimeTimer.hasPassed(20)
                    && data.playerInfo.blockAboveTimer.hasPassed(6)
                    && !data.blockInfo.collidesHorizontally
                    && data.playerInfo.lastHalfBlock.hasPassed(3)
                    && data.playerInfo.lastVelocity.hasPassed(10)
                    && !data.playerInfo.serverGround
                    && (data.playerInfo.blockAboveTimer.hasPassed(5) || data.playerInfo.deltaY >= 0)
                    && MathUtils.getDelta(data.playerInfo.deltaY, predicted) > 0.0001) {
                vl++;
                if(vl > (data.lagInfo.lagging ? 3 : 2)) {
                    flag("deltaY=%1 predicted=%2", data.playerInfo.deltaY, predicted);
                }
            } else vl-= vl > 0 ? 0.2f : 0;

            debug("deltaY=" + data.playerInfo.deltaY + " predicted=" + predicted
                    + " ground=" + data.playerInfo.clientGround + " vl=" + vl);
        }
    }
}
