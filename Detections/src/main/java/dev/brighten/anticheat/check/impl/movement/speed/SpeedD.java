package dev.brighten.anticheat.check.impl.movement.speed;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Optional;

@CheckInfo(name = "Speed (D)", description = "Clip check", executable = true, punishVL = 20, cancellable = true)
@Cancellable
public class SpeedD extends Check {

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long now) {
        if(!packet.isPos() || now - data.creation < 800L || now - data.playerInfo.lastRespawn < 500L
                || data.playerInfo.lastTeleportTimer.isPassed(1)
                || data.playerInfo.doingTeleport ||data.playerInfo.canFly || data.playerInfo.creative
                || data.playerInfo.vehicleTimer.isNotPassed(2)) return;

        double threshold = data.potionProcessor.hasPotionEffect(PotionEffectType.JUMP) ? 0.62 : 0.5;

        if(data.blockInfo.pistonNear) threshold = 0.95;
        else if(data.playerInfo.blockAboveTimer.isNotPassed(20)) {
            //TODO Fix under block falses
            threshold = 0.8;
            if(data.playerInfo.iceTimer.isNotPassed(20)) threshold+= 0.4;
        }
        else if(data.playerInfo.jumped) threshold = 0.68;
        else if(data.playerInfo.iceTimer.isNotPassed(4)) threshold = 0.6;

        if(data.playerInfo.lastVelocity.isNotPassed(20))
            threshold = Math.max(threshold, data.playerInfo.velocityXZ + 0.3);

        Optional<PotionEffect> speed = data.potionProcessor.getEffectByType(PotionEffectType.SPEED);

        if(speed.isPresent()) {
            threshold*= 1.2 * (speed.get().getAmplifier() + 1);
        }

        if(data.playerInfo.deltaXZ > threshold) {
            vl++;
            flag(80, "%.3f>-%.3f", data.playerInfo.deltaXZ, threshold);
        }
    }
}
