package dev.brighten.anticheat.check.impl.movement.fly;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.math.cond.MaxDouble;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;

@CheckInfo(name = "Fly (J)", description = "A check designed to fix simple elytra flys.", checkType = CheckType.FLIGHT, devStage = DevStage.ALPHA, punishVL = 8)
@Cancellable
public class FlyJ extends Check {

    private final MaxDouble buffer = new MaxDouble(10);

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if (!packet.isPos()
                || data.blockInfo.inLiquid
                || !data.playerInfo.gliding) return;

        final double deltaYabs = Math.abs(data.playerInfo.deltaY);
        final double lDeltaYabs = Math.abs(data.playerInfo.lDeltaY);
        final double accel = Math.abs(deltaYabs - lDeltaYabs);

        if ((data.playerInfo.deltaY > 0 && accel <= 0.00001) || (deltaYabs <= 0.001 && deltaYabs == lDeltaYabs)) {
            if (buffer.add() > 5) {
                flag();
            }
        } else {
            buffer.subtract(0.5);
        }

        debug("deltaY=%s lDeltaY=%s accel=%s buffer=%s", deltaYabs, lDeltaYabs, accel, buffer.value());
    }
}
