package dev.brighten.anticheat.check.impl.movement.fly;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.Color;
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
        if(packet.isPos()) {
            //We check if the player is in ground, since theoretically the y should be zero.
            double lDeltaY = data.playerInfo.lClientGround ? 0 : data.playerInfo.lDeltaY;
            double predicted = (lDeltaY - 0.08) * mult;

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

            boolean flagged = false;
            if(!data.playerInfo.flightCancel
                    && !data.playerInfo.serverPos
                    && data.playerInfo.lastVelocity.isPassed(3)
                    && !data.playerInfo.serverGround
                    && data.playerInfo.blockAboveTimer.isPassed(5)
                    && deltaPredict > 0.016) {
                flagged = true;
                if(++buffer > 2 || data.playerInfo.kAirTicks > 80) {
                    ++vl;
                    flag("dY=%.3f p=%.3f dx=%.3f", data.playerInfo.deltaY, predicted, data.playerInfo.deltaXZ);
                }
            } else buffer-= buffer > 0 ? 0.5f : 0;

            debug((flagged ? Color.Green : "") +"pos=%s deltaY=%.3f predicted=%.3f ground=%s lpass=%s air=%s buffer=%.1f",
                    packet.getY(), data.playerInfo.deltaY, predicted, data.playerInfo.clientGround,
                    data.playerInfo.liquidTimer.getPassed(), data.playerInfo.kAirTicks, buffer);
            lastPos = timeStamp;
        }
    }
}