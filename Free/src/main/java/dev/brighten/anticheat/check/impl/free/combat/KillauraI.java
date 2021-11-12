package dev.brighten.anticheat.check.impl.free.combat;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.KauriVersion;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;

@CheckInfo(name = "Killaura (I)", description = "Looks for suspicious accuracy", checkType = CheckType.KILLAURA,
        devStage = DevStage.CANARY, planVersion = KauriVersion.FREE)
public class KillauraI extends Check {

    private int arm, useEntity, buffer;

    @Packet
    public void onUse(WrappedInUseEntityPacket packet) {
        useEntity++;
    }

    @Packet
    public void onArm(WrappedInArmAnimationPacket packet) {
        if(++arm >= 20) {
            float ratio = useEntity / (float)arm;

            if(ratio > 0.99f) {
                if(++buffer > 4) {
                    vl++;
                    flag("r=%.1f%% b=%s", ratio * 100f, buffer);
                }
            } else buffer = 0;

            debug("ratio=%.2f b=%s", ratio, buffer);

            arm = useEntity = 0;
        }
    }
}
