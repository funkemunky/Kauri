package dev.brighten.anticheat.check.impl.combat.killaura;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;

@CheckInfo(name = "Killaura (I)", description = "Looks for suspicious accuracy", checkType = CheckType.KILLAURA,
        devStage = DevStage.BETA)
public class KillauraI extends Check {

    private int arm, useEntity, buffer, validAmount;

    @Packet
    public void onUse(WrappedInUseEntityPacket packet) {
        useEntity++;
    }

    @Packet
    public void onArm(WrappedInArmAnimationPacket packet) {
        if(data.playerInfo.deltaXZ > 0.21 && data.target != null
                && MathUtils.getDelta(data.target.getVelocity().getY(), -0.078) > 0.001) validAmount++;
        if(++arm >= 14) {
            float ratio = useEntity / (float)arm;

            if(ratio > 0.99f) {
                if(validAmount > 6 && ++buffer > 4) {
                    vl++;
                    flag("r=%.1f%% b=%s", ratio * 100f, buffer);
                }
            } else buffer = 0;

            debug("ratio=%.2f b=%s v=%s", ratio, buffer, validAmount);

            arm = validAmount = useEntity = 0;
        }
    }
}
