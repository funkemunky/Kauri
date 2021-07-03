package dev.brighten.anticheat.check.impl.movement.general;

import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Event;
import dev.brighten.anticheat.utils.MiscUtils;
import dev.brighten.api.check.CheckType;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffectType;

@CheckInfo(name = "OmniSprint", description = "Checks for sprinting in illegal directions.",
        checkType = CheckType.GENERAL, vlToFlag = 15, punishVL = 50, developer = true)
@Cancellable
public class OmniSprint extends Check {

    private int sprintBuffer, invalidBuffer;

    @Event
    public void onPlayerMove(PlayerMoveEvent event) {
        double angle = MiscUtils.getAngle(event.getTo().clone(), event.getFrom().clone());
        double deltaXZ = MathUtils.getHorizontalDistance(event.getTo(), event.getFrom());

        if(Math.abs(angle) > 95 || data.predictionService.key.contains("W")) return;

        omniSprint: {
            if(!event.getPlayer().isSprinting() || !data.playerInfo.serverGround) break omniSprint;

            if(deltaXZ > 0.2
                    && !data.playerInfo.doingVelocity
                    && !data.playerInfo.generalCancel
                    && data.playerInfo.lastVelocity.isPassed(20)) {
                if(++sprintBuffer > 3) {
                    vl++;
                    flag("type=SPRINT a=%.1f b=%s", angle, sprintBuffer);
                }
            }
        }

        invalidMove: {
            if(data.playerInfo.groundTicks < 9 || !data.playerInfo.serverGround) break invalidMove;

            double base = .2161;
            int speed = data.potionProcessor.getEffectByType(PotionEffectType.SPEED)
                    .map(ef -> ef.getAmplifier() + 1).orElse(0);

            if(speed > 0)
            base *= 1.2 * speed;

            if(deltaXZ > base
                    && data.playerInfo.lastVelocity.isPassed(20)
                    && data.playerInfo.lastTeleportTimer.isPassed(1)) {
                if(++invalidBuffer > 6) {
                    vl+= 0.25f;
                    flag("type=INVALID sprint=%s a=%.1f b=%s", angle, invalidBuffer);
                }
            } else invalidBuffer = 0;

            debug("dxz=%.5f base=%.5f", deltaXZ, base);
        }
    }

    @Event
    public void onTeleport(PlayerTeleportEvent event) {
        event.getPlayer().setSprinting(false);
    }

}
