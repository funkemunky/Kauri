package dev.brighten.anticheat.check.impl.movement.fly;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Fly (B)", description = "Checks for improper acceleration.", checkType = CheckType.FLIGHT,
        vlToFlag = 4, punishVL = 12, developer = true)
@Cancellable
public class FlyB extends Check {

    private double predicted;

    @Packet
    public void onVelocity(WrappedOutVelocityPacket packet) {
        data.runKeepaliveAction(d -> {
            predicted = packet.getY();
        });
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        if(packet.isPos()) {
            //We check if the player is in ground, since theoretically the y should be zero.

            if(data.playerInfo.lClientGround && !data.playerInfo.clientGround
                    && (data.playerInfo.deltaY > 0 && data.playerInfo.deltaY < data.playerInfo.jumpHeight * 1.5)) {
                predicted = data.playerInfo.jumpHeight;
            } else if(data.playerInfo.lClientGround
                    && !data.playerInfo.clientGround && data.playerInfo.lDeltaY == 0 && data.playerInfo.deltaY < 0) {
                predicted = data.playerInfo.deltaY;
            }

            boolean usedCollision = false;

            for (SimpleCollisionBox sBox : data.blockInfo.belowCollisions) {
                double minDelta = sBox.yMax - data.playerInfo.from.y;

                if(MathUtils.getDelta(data.playerInfo.deltaY, minDelta) < 1E-7) {
                    predicted = minDelta;
                    usedCollision = true;
                    break;
                }
            }

            for (SimpleCollisionBox sBox : data.blockInfo.aboveCollisions) {
                double maxDelta = sBox.yMin - (data.playerInfo.from.y + 1.8);

                if(MathUtils.getDelta(data.playerInfo.deltaY, maxDelta) < 1E-7) {
                    predicted = maxDelta;
                    usedCollision = true;
                    break;
                }
            }

            if((data.playerInfo.clientGround && predicted > -0.08 && predicted < -0.077)
                    || (Math.abs(predicted) < 0.005 && data.playerVersion.isOrBelow(ProtocolVersion.V1_8_9))) {
                predicted = 0;
            }

            double deltaPredict =  MathUtils.getDelta(data.playerInfo.deltaY, predicted);

            if(!data.playerInfo.flightCancel
                    && (MathUtils.getDelta(-0.098, data.playerInfo.deltaY) > 0.001 || data.playerInfo.deltaXZ > 0.3)
                    && !(data.playerInfo.blockOnTo != null && data.playerInfo.blockOnTo.getType().isSolid())
                    && (!data.blockInfo.blocksAbove || data.playerInfo.deltaY >= 0)
                    && data.playerInfo.slimeTimer.hasPassed(20)
                    && timeStamp - data.playerInfo.lastServerPos > 100L
                    && data.playerInfo.liquidTimer.hasPassed(8)
                    && data.playerInfo.lastBlockPlace.hasPassed(8)
                    && data.playerInfo.lastVelocity.hasPassed(10)
                    && deltaPredict > 0.0001) {
                if(++vl > (data.lagInfo.lastPacketDrop.hasPassed(5) ? 2.5 : 4)) {
                    flag("dY=%v.3 p=%v.3 dx=%v.3", data.playerInfo.deltaY, predicted, data.playerInfo.deltaXZ);
                }
            } else vl-= vl > 0 ? 0.2f : 0;

            debug("pos=%v deltaY=%v.3 collided=%v predicted=%v.3 ground=%v lpass=%v vl=%v.1",
                    packet.getY(), data.playerInfo.deltaY, usedCollision, predicted, data.playerInfo.clientGround,
                    data.playerInfo.liquidTimer.getPassed(), vl);

            predicted-= 0.08;
            predicted*= (double)0.98f;
        } else {
            if(data.playerInfo.deltaXZ == 0 && data.playerInfo.lDeltaY != 0 && !data.playerInfo.clientGround) {
                if(Math.abs(predicted) < 0.005 && data.playerVersion.isBelow(ProtocolVersion.V1_9)) predicted = 0;
                predicted-= 0.08;
                predicted*= (double)0.98f;
            } else predicted = 0;
        }
    }
}
