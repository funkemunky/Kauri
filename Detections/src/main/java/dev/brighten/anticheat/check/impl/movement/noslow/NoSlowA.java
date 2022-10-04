package dev.brighten.anticheat.check.impl.movement.noslow;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.TickTimer;
import cc.funkemunky.api.utils.math.cond.MaxDouble;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Optional;

@CheckInfo(name = "NoSlow (A)", description = "Checks for invalid web movements.", executable = true,
        checkType = CheckType.NOSLOW, devStage = DevStage.ALPHA)
@Cancellable
public class NoSlowA extends Check {

    private final MaxDouble maxDouble = new MaxDouble(5);
    private TickTimer lastJumped = new TickTimer(10);

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if (!packet.isPos()
                || data.playerInfo.generalCancel
                || data.playerInfo.inWebTimer.isNotPassed(20))
            return;

        final double deltaXZ = data.playerInfo.deltaXZ;
        double threshold = 0.0325;

        if (data.playerInfo.jumped) {
            lastJumped.reset();
            threshold += 0.06;
        } else if (lastJumped.hasNotPassed(10)) {
            threshold += 0.06;
        }

        Optional<PotionEffect> speed = data.potionProcessor.getEffectByType(PotionEffectType.SPEED);

        if (speed.isPresent()) {
            threshold *= 1.2 * (speed.get().getAmplifier() + 1);
        }

        if (deltaXZ > threshold) {
            if (maxDouble.add() > 2) {
                flag("deltaXZ=%.4f threshold=%.4f", deltaXZ, threshold);
            }
        } else {
            maxDouble.subtract(0.025);
        }

        debug("dxz=%s threshold=%s ljump=%s",
            deltaXZ, threshold, lastJumped.getPassed());
    }
}
