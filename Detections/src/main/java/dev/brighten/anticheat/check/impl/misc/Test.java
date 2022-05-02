package dev.brighten.anticheat.check.impl.misc;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.DevStage;

@CheckInfo(name = "Test", devStage = DevStage.ALPHA)
public class Test extends Check {

    private float lyaw;
    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(!packet.isLook()) return;

        float yaw = packet.getYaw();

        float calced = calculate(yaw, lyaw);

        debug("calc=%.4f s=%.4f dx=%.4f", calced, data.moveProcessor.currentSensX,
                data.moveProcessor.deltaX);
    }

    private static float calculate(float ny, float oy) {
        float result = ny - oy;
        result/= 0.15f;
        result/= 8f;
        result-= 0.2f;
        result/= 0.6f;

        return result;
    }
}
