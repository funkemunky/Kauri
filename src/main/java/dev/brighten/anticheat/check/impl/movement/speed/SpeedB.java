package dev.brighten.anticheat.check.impl.movement.speed;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.reflection.MinecraftReflection;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.types.WrappedEnumAnimation;
import cc.funkemunky.api.utils.MathHelper;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.PlayerUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.MovementUtils;
import org.bukkit.potion.PotionEffectType;

@CheckInfo(name = "Speed (B)", description = "Ensures horizontal acceleration is legit.", punishVL = 50)
public class SpeedB extends Check {

    private double lMotionXZ, lPAccel;
    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(!packet.isPos() ||
                data.playerInfo.generalCancel
                || data.playerInfo.lastVelocity.hasNotPassed(5)
                || data.playerInfo.liquidTicks > 0
                || data.playerInfo.webTicks > 0
                || Atlas.getInstance().getBlockBoxManager().getBlockBox().isUsingItem(data.getPlayer())
                || data.blockInfo.blocksAbove
                || data.blockInfo.blocksNear
                || data.playerInfo.halfBlockTicks > 0) {
            vl-= vl > 0 ? 0.25 : 0;
            return;
        }

        double motionXZ = MathUtils.hypot(data.predictionService.rmotionX, data.predictionService.rmotionZ);
        double pAccel = motionXZ - lMotionXZ;
        if (data.playerInfo.airTicks > 1) {
            double accel = data.playerInfo.deltaXZ - data.playerInfo.lDeltaXZ;
            float delta = (float) MathUtils.getDelta(lPAccel, accel);
            if(delta > 0.001) {
                if(vl++ > 6) {
                    flag("delta=" + delta);
                }
            } else vl-= vl > 0 ? 1 : 0;
            debug("p=" + MathUtils.round(lPAccel, 5) + " a=" + MathUtils.round(accel, 5));
        }
        lPAccel = pAccel;
        lMotionXZ = motionXZ;
    }
}
