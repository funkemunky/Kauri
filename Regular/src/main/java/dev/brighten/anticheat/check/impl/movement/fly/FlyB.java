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

@CheckInfo(name = "Fly (B)", description = "Checks for improper acceleration.", checkType = CheckType.FLIGHT,
        vlToFlag = 4, punishVL = 30)
@Cancellable
public class FlyB extends Check {

    private long lastPos;
    private float buffer;
    private static double mult = 0.98f;
    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        if(packet.isPos() && (data.playerInfo.deltaY != 0 || data.playerInfo.deltaXZ != 0)) {
            //We check if the player is in ground, since theoretically the y should be zero.
            double predicted = (data.playerInfo.lDeltaY - 0.08) * mult;

            if(data.playerInfo.lClientGround && !data.playerInfo.clientGround && data.playerInfo.deltaY > 0) {
                predicted = MovementUtils.getJumpHeight(data);
            }

            //Basically, this bug would only occur if the client's movement is less than a certain amount.
            //If it is, it won't send any position packet. Usually this only occurs when the magnitude
            //of motionY is less than 0.005 and it rounds it to 0.
            //The easiest way I found to produce this oddity is by putting myself in a corner and just jumping.
            if(Math.abs(data.playerInfo.deltaY) < 0.005
                    && ProtocolVersion.getGameVersion().isBelow(ProtocolVersion.V1_9)) {
                predicted = 0;
            }

            if(timeStamp - lastPos > 60L) {
                double toCheck = (predicted - 0.08) * mult;

                if(Math.abs(data.playerInfo.deltaY - toCheck) < Math.abs(data.playerInfo.deltaY - predicted))
                    predicted = toCheck;
            }

            double deltaPredict = MathUtils.getDelta(data.playerInfo.deltaY, predicted);

            if(!data.playerInfo.flightCancel
                    && data.playerInfo.lastVelocity.hasPassed(3)
                    && (!data.playerInfo.clientGround || data.playerInfo.deltaY < predicted)
                    && data.playerInfo.blockAboveTimer.hasPassed(1)
                    && deltaPredict > 0.016) {
                if(++buffer > 2 || (deltaPredict > 0.1 && !data.blockInfo.blocksNear)) {
                    ++vl;
                    flag("dY=%v.3 p=%v.3 dx=%v.3", data.playerInfo.deltaY, predicted, data.playerInfo.deltaXZ);
                }
            } else buffer-= buffer > 0 ? 0.2f : 0;

            debug("pos=%v deltaY=%v.3 predicted=%v.3 ground=%v lpass=%v vl=%v.1",
                    packet.getY(), data.playerInfo.deltaY, predicted, data.playerInfo.clientGround,
                    data.playerInfo.liquidTimer.getPassed(), vl);
            lastPos = timeStamp;
        }
    }
}