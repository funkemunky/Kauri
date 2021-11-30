package dev.brighten.anticheat.check.impl.free.movement.fly;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.MovementUtils;
import dev.brighten.api.KauriVersion;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Fly (A)", description = "Checks for improper acceleration.", checkType = CheckType.FLIGHT,
        vlToFlag = 4, punishVL = 30, planVersion = KauriVersion.FREE, executable = true)
@Cancellable
public class FlyA extends Check {

    private long lastPos;
    private float buffer;
    private static double mult = 0.98f;
    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        if(packet.isPos() && (data.playerInfo.deltaXZ > 0 || data.playerInfo.deltaY != 0)) {
            //We check if the player is in ground, since theoretically the y should be zero.
            double lDeltaY = data.playerInfo.lClientGround ? 0 : data.playerInfo.lDeltaY;
            boolean onGround = data.playerInfo.clientGround && data.blockInfo.blocksBelow;
            double predicted = onGround ? lDeltaY : (lDeltaY - 0.08) * mult;

            if(data.playerInfo.lClientGround && !onGround && data.playerInfo.deltaY > 0) {
                predicted = MovementUtils.getJumpHeight(data);
            }

            //Basically, this bug would only occur if the client's movement is less than a certain amount.
            //If it is, it won't send any position packet. Usually this only occurs when the magnitude
            //of motionY is less than 0.005 and it rounds it to 0.
            //The easiest way I found to produce this oddity is by putting myself in a corner and just jumping.
            if(Math.abs(predicted) < 0.005
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
                    && data.playerInfo.lastBlockPlace.isPassed(5)
                    && data.playerInfo.lastVelocity.isPassed(3)
                    && !data.playerInfo.serverGround
                    && data.playerInfo.climbTimer.isPassed(15)
                    && data.playerInfo.blockAboveTimer.isPassed(5)
                    && deltaPredict > 0.016) {
                flagged = true;
                if(++buffer > 2 || data.playerInfo.kAirTicks > 80) {
                    ++vl;
                    flag("dY=%.3f p=%.3f dx=%.3f", data.playerInfo.deltaY, predicted,
                            data.playerInfo.deltaXZ);
                    fixMovementBugs();
                }
            } else buffer-= buffer > 0 ? 0.5f : 0;

            debug((flagged ? Color.Green : "")
                            +"pos=%s deltaY=%.3f predicted=%.3f d=%.3f ground=%s lpass=%s cp=%s air=%s buffer=%.1f sg=%s cb=%s fc=%s ",
                    packet.getY(), data.playerInfo.deltaY, predicted, deltaPredict, onGround,
                    data.playerInfo.liquidTimer.getPassed(), data.playerInfo.climbTimer.getPassed(),
                    data.playerInfo.kAirTicks, buffer, data.playerInfo.serverGround,
                    data.playerInfo.climbTimer.getPassed(), data.playerInfo.flightCancel);
            lastPos = timeStamp;
        }
    }
}