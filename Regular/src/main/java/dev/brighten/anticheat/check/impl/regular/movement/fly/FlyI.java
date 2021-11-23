package dev.brighten.anticheat.check.impl.regular.movement.fly;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;

@CheckInfo(name = "Fly (I)", description = "Checks for invalid jump heights", checkType = CheckType.FLIGHT,
        devStage = DevStage.ALPHA)
@Cancellable
public class FlyI extends Check {

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(!packet.isPos()) return;

        if(data.playerInfo.jumped && !data.blockInfo.blocksAbove
                && !data.playerInfo.generalCancel
                && (!data.playerInfo.serverGround || !data.blockInfo.onHalfBlock)
                && data.playerInfo.liquidTimer.isPassed(3)
                && data.playerInfo.lastVelocity.isPassed(2)
                && data.playerInfo.climbTimer.isPassed(3)) {
            double delta = Math.abs(data.playerInfo.jumpHeight - data.playerInfo.deltaY);

            if(delta > 0.0001) {
                vl++;
                flag("j=%.4f dy=%.4f d=%.4f", data.playerInfo.jumpHeight, data.playerInfo.deltaY, delta);
            }

            debug("delta=%s", delta);
        }
    }
}
