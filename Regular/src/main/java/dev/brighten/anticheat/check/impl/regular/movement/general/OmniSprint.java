package dev.brighten.anticheat.check.impl.regular.movement.general;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.api.check.CheckType;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffectType;

@CheckInfo(name = "OmniSprint", description = "Checks for sprinting in illegal directions.",
        checkType = CheckType.GENERAL, vlToFlag = 15, punishVL = 50)
@Cancellable
public class OmniSprint extends Check {

    private int sprintBuffer, invalidBuffer;

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {

        if(Math.abs(data.predictionService.motionYaw) > 95 || data.predictionService.key.contains("W")) return;

        omniSprint: {
            if(!data.playerInfo.sprinting || !data.playerInfo.serverGround) break omniSprint;

            if(data.playerInfo.deltaXZ > 0.2
                    && !data.playerInfo.doingVelocity
                    && !data.playerInfo.generalCancel
                    && data.playerInfo.lastVelocity.isPassed(20)) {
                if(++sprintBuffer > 3) {
                    vl++;
                    flag("type=SPRINT a=%.1f b=%s", data.predictionService.motionYaw, sprintBuffer);
                }
            }
        }

        invalidMove: {
            if(data.playerInfo.groundTicks < 9 || !data.playerInfo.serverGround) break invalidMove;

            double base = .235;
            int speed = data.potionProcessor.getEffectByType(PotionEffectType.SPEED)
                    .map(ef -> ef.getAmplifier() + 1).orElse(0);

            if(speed > 0)
            base *= 1.2 * speed;

            if(data.playerInfo.deltaXZ > base
                    && data.playerInfo.lastVelocity.isPassed(20)
                    && data.playerInfo.lastTeleportTimer.isPassed(1)) {
                if(++invalidBuffer > 6) {
                    vl+= 0.25f;
                    flag("type=INVALID sprint=%s a=%.1f b=%s delta=%.6f>-%.6f",
                            data.playerInfo.sprinting, data.predictionService.motionYaw, invalidBuffer,
                            data.playerInfo.deltaXZ, base);
                }
            } else invalidBuffer = 0;

            debug("dxz=%.5f base=%.5f", data.playerInfo.deltaXZ, base);
        }
    }

    @Event
    public void onTeleport(PlayerTeleportEvent event) {
        event.getPlayer().setSprinting(false);
    }

}
