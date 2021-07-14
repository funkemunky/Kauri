package dev.brighten.anticheat.check.impl.movement.fly;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.Color;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.KauriVersion;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Fly (A)", description = "Simple fly check.", punishVL = 10,
        checkType = CheckType.FLIGHT, vlToFlag = 3, developer = true, planVersion = KauriVersion.FREE)
@Cancellable
public class FlyA extends Check {

    private static double GROUND = 1 / 64d, CHUNK_LOAD = -0.1 * 0.98D;

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        if(!packet.isPos() || data.playerInfo.lastTeleportTimer.isNotPassed(1)
                || data.playerInfo.flightCancel
                || data.playerInfo.lastVelocity.isNotPassed(3)
                || data.playerInfo.blockAboveTimer.isNotPassed(3)
                || data.playerInfo.lastRespawnTimer.isNotPassed(5)) return;

        long start = System.nanoTime();

        boolean hitHead = (packet.getY() + 1.8f) % GROUND <= 1E-8;

        if(Math.abs(data.playerInfo.deltaY - CHUNK_LOAD) < 1E-5) {
            debug("chunk isn't loaded");
            return;
        }

        double lDeltaY = data.playerInfo.lDeltaY;

        long end = -1;
        if(!data.playerInfo.nearGround && data.playerInfo.airTicks > 1) {
            double predicted = (lDeltaY - 0.08) * (double)0.98f;

            if(Math.abs(predicted) < 0.005) {
                if(data.playerVersion.isBelow(ProtocolVersion.V1_9))
                predicted = 0;
                double last = predicted;
                predicted-= 0.08;
                predicted*= 0.98f;
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
                flag("deltaY=%.4f predicted=%.4f", data.playerInfo.deltaY, predicted);
                fixMovementBugs();
            } else if(vl > 0) vl-= 0.25;
            end = System.nanoTime() - start;

            debug(Color.Green + "deltaY=%s difference=%s", data.playerInfo.deltaY, check);
        }

        debug("hitHead=%s time=%s", hitHead, end);
    }
}
