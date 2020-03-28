package dev.brighten.anticheat.check.impl.movement.fly;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInKeepAlivePacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.Helper;
import dev.brighten.anticheat.utils.MovementUtils;
import dev.brighten.anticheat.utils.Verbose;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Fly (D)", description = "Ensures a user doesn't fly faster than the maximum threshold.",
        checkType = CheckType.FLIGHT, punishVL = 10)
@Cancellable
public class FlyD extends Check {
    private Verbose verbose = new Verbose(30, 5);

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(packet.isPos()) {
            double threshold = Math.max(0.8, data.playerInfo.jumpHeight * 2);

            if(data.playerInfo.deltaY > threshold
                    && !data.playerInfo.canFly
                    && !data.playerInfo.flightCancel
                    && !data.playerInfo.creative
                    && !data.playerInfo.wasOnSlime) {
                vl++;
                flag("deltaY=%v;threshold=%v type=deltaY",
                        Helper.format(data.playerInfo.deltaY, 2), Helper.format(threshold, 2));
            }

            double delta = data.playerInfo.deltaY - data.playerInfo.lDeltaY;
            if(!data.playerInfo.flightCancel
                    && Math.abs(delta) > 0.185
                    && (!data.playerInfo.lClientGround || !data.playerInfo.clientGround)
                    && data.playerInfo.lastVelocity.hasPassed(2)
                    && data.playerInfo.lastHalfBlock.hasPassed(10)
                    && data.playerInfo.blockAboveTimer.hasPassed(10)
                    && verbose.flag(1, 4)) {
                vl++;
                flag("%v>-0.185 type=accel", MathUtils.round(delta, 3));
            }

            if(!data.playerInfo.flightCancel
                    && delta > 0.01
                    && data.playerInfo.lastVelocity.hasPassed(2)
                    && !data.playerInfo.nearGround
                    && !data.playerInfo.clientGround
                    && !data.playerInfo.lClientGround) {
                vl++;
                flag("deltaY=%v.2", delta);
            }
        }
    }
}
