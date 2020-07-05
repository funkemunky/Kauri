package dev.brighten.anticheat.check.impl.movement.fly;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import cc.funkemunky.api.utils.Color;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.MovementUtils;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Fly (A)", description = "Simple fly check.", punishVL = 5,
        checkType = CheckType.FLIGHT, developer = true)
@Cancellable
public class FlyA extends Check {

    private static double GROUND = 1 / 64d, CHUNK_LOAD = -0.1 * 0.98D;

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(!packet.isPos() || data.playerInfo.lastTeleportTimer.hasNotPassed(1)
                || data.playerInfo.flightCancel
                || data.playerInfo.lastVelocity.hasNotPassed(2)
                || data.playerInfo.blockAboveTimer.hasNotPassed(3)
                || data.playerInfo.lastRespawnTimer.hasNotPassed(5)) return;

        long start = System.nanoTime();
        boolean onGround = packet.getY() % GROUND == 0, fromGround = data.playerInfo.from.y % GROUND == 0;

        boolean hitHead = (packet.getY() + 1.8f) % GROUND <= 1E-8;

        if(Math.abs(data.playerInfo.deltaY - CHUNK_LOAD) < 1E-5) {
            debug("chunk isn't loaded");
            return;
        }

        long end = -1;
        if(!onGround && !fromGround && !hitHead) {
            double predicted = (data.playerInfo.lDeltaY - 0.08) * (double)0.98f;

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

            if(check > 0.05) {
                vl++;
                flag("deltaY=%v.4 predicted=%v.4", data.playerInfo.deltaY, predicted);
            }
            end = System.nanoTime() - start;

            debug(Color.Green + "deltaY=%v difference=%v", data.playerInfo.deltaY, check);
        }

        debug("ground=%v fground=%v hitHead=%v time=%v",
                onGround, fromGround, hitHead, end);
    }
}
