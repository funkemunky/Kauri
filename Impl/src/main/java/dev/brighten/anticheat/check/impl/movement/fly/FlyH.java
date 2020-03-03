package dev.brighten.anticheat.check.impl.movement.fly;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.Verbose;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Fly (H)", description = "Checks for oddly large difference in vertical movement.",
        checkType = CheckType.FLIGHT, developer = true, enabled = false, vlToFlag = 1)
public class FlyH extends Check {

    private Verbose verbose = new Verbose(30, 5);
    @Packet
    public void onPacket(WrappedInFlyingPacket packet) {
        if(packet.isPos()) {
            double delta = Math.abs(data.playerInfo.deltaY - data.playerInfo.lDeltaY);

            if(!data.playerInfo.flightCancel
                    && delta > 0.16
                    && data.playerInfo.lastVelocity.hasPassed(2)
                    && data.playerInfo.lastHalfBlock.hasPassed(10)
                    && data.playerInfo.blockAboveTimer.hasPassed(10)
                    && verbose.flag(1, 4)) {
                vl++;
                flag("%1>-0.16", MathUtils.round(delta, 3));
            }
            debug("delta=%1 ground=%2", delta, packet.isGround());
        }
    }
}
