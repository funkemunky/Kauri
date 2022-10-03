package dev.brighten.anticheat.check.impl.movement.general;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.anticheat.utils.MiscUtils;
import dev.brighten.api.check.CheckType;
import org.bukkit.GameMode;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffectType;

@CheckInfo(name = "OmniSprint", description = "Checks for sprinting in illegal directions.",
        checkType = CheckType.GENERAL, vlToFlag = 15, punishVL = 20)
@Cancellable
public class OmniSprint extends Check {

    private float sprintBuffer, invalidBuffer;

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        double angle = Math.abs(MiscUtils.getAngle(data.playerInfo.to, data.playerInfo.from));

        if (angle > 100.0D || data.predictionService.key.contains("W")
                || (data.playerInfo.lastEntityCollision.isNotPassed(4)
                    && data.playerVersion.isOrAbove(ProtocolVersion.V1_9)))
            return;

        omniSprint: {
            if (!data.playerInfo.sprinting || !data.playerInfo.serverGround)
                break omniSprint;

            if (data.playerInfo.deltaXZ > 0.2
                    && !data.blockInfo.onClimbable && !data.blockInfo.onSlime
                    && !data.blockInfo.inWeb && !data.blockInfo.inLiquid
                    && !data.blockInfo.miscNear && !data.blockInfo.onHalfBlock
                    && !data.playerInfo.generalCancel
                    && data.playerInfo.lastAttack.isPassed(2)
                    && data.playerInfo.lastVelocity.isPassed(2)) {
                if (++sprintBuffer > 3) {
                    vl++;
                    flag("type=SPRINT a=%.1f b=%s", angle, sprintBuffer);
                }
            } else if (sprintBuffer > 0)
                sprintBuffer -= Math.min(sprintBuffer, 0.25);
        }

        invalidMove: {
            if (data.playerInfo.groundTicks < 9 || !data.playerInfo.serverGround
                    || data.getPlayer().getGameMode().equals(GameMode.SPECTATOR))
                break invalidMove;

            double base = .235;
            int speed = data.potionProcessor.getEffectByType(PotionEffectType.SPEED)
                    .map(ef -> ef.getAmplifier() + 1).orElse(0);

            if (speed > 0)
                base *= 1.2 * speed;

            if (data.playerInfo.deltaXZ > base
                    && data.playerInfo.lastVelocity.isPassed(20)
                    && data.playerInfo.lastTeleportTimer.isPassed(1)) {
                if (++invalidBuffer > 6) {
                    vl += 0.25f;
                    flag("type=INVALID sprint=%s a=%.1f b=%s delta=%.6f>-%.6f",
                        data.playerInfo.sprinting, data.predictionService.motionYaw, invalidBuffer,
                        data.playerInfo.deltaXZ, base);
                }
            } else invalidBuffer = 0;

            debug("dxz=%.5f base=%.5f sprint=%s", data.playerInfo.deltaXZ, base, data.playerInfo.sprinting);
        }
    }

    @Event
    public void onTeleport(PlayerTeleportEvent event) {
        event.getPlayer().setSprinting(false);
    }
}
