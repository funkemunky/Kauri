package dev.brighten.anticheat.check.impl.movement.fly;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.Color;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Fly (A)", description = "Simple fly check.", punishVL = 10,
        checkType = CheckType.FLIGHT, vlToFlag = 3, developer = true)
@Cancellable
public class FlyA extends Check {

    private static double GROUND = 1 / 64d, CHUNK_LOAD = -0.1 * 0.98D;

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        if(!packet.isPos() || data.playerInfo.lastTeleportTimer.isNotPassed(1)
                || data.playerInfo.flightCancel
                || timeStamp - data.playerInfo.lastVelocityTimestamp <= 200L
                || data.playerInfo.lastVelocity.isNotPassed(2)
                || data.playerInfo.blockAboveTimer.isNotPassed(3)
                || data.playerInfo.lastRespawnTimer.isNotPassed(5)) return;

        long start = System.nanoTime();

        boolean hitHead = (packet.getY() + 1.8f) % GROUND <= 1E-8;

        if(Math.abs(data.playerInfo.deltaY - CHUNK_LOAD) < 1E-5) {
            debug("chunk isn't loaded");
            return;
        }

        boolean ground = data.playerInfo.clientGround;
        boolean lground = data.playerInfo.lClientGround;
        
        double lDeltaY = data.playerInfo.lClientGround ? 0 : data.playerInfo.lDeltaY;

        long end = -1;
        if(!ground && !hitHead) {
            double predicted = (lDeltaY - 0.08) * (double)0.98f;

            if(lground) {
                if(data.playerInfo.deltaY > 0) {
                    predicted = data.playerInfo.blockAboveTimer.isNotPassed(3)
                            ? Math.min(data.playerInfo.deltaY, data.playerInfo.jumpHeight)
                            : data.playerInfo.jumpHeight;
                } else {
                    double toCheck = (predicted - 0.08) * (double)0.98f;

                    if(Math.abs(toCheck - data.playerInfo.deltaY)
                            < Math.abs(predicted - data.playerInfo.deltaY)) {
                        predicted = toCheck;
                    }
                }
            }

            if(Math.abs(predicted) < 0.005) {
                if(data.playerVersion.isBelow(ProtocolVersion.V1_9))
                predicted = 0;
                double last = predicted;
                predicted-= 0.08;
                predicted*= (double)0.98f;
                if(Math.abs(data.playerInfo.deltaY - predicted) > Math.abs(last - data.playerInfo.deltaY))
                    predicted = last;
            }

            double check = Math.abs(data.playerInfo.deltaY - predicted);

            if(check > 0.016
                    && data.playerInfo.slimeTimer.isPassed(10)
                    && !data.playerInfo.serverGround
                    && data.playerInfo.lastHalfBlock.isPassed(5)
                    && data.playerInfo.lastVelocity.isPassed(4)) {
                vl++;
                if(vl > 3)
                flag("deltaY=%v.4 predicted=%v.4", data.playerInfo.deltaY, predicted);
            } else if(vl > 0) vl-= 0.25;
            end = System.nanoTime() - start;

            debug(Color.Green + "deltaY=%v difference=%v", data.playerInfo.deltaY, check);
        }

        debug("ground=%v fground=%v hitHead=%v time=%v", ground, lground, hitHead, end);
    }
}
