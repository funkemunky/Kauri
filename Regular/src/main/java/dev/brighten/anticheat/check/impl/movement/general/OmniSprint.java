package dev.brighten.anticheat.check.impl.movement.general;

import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.*;
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

        if(Math.abs(angle) > 95) return;

        omniSprint: {
            if(!event.getPlayer().isSprinting() || !data.playerInfo.serverGround) break omniSprint;

            if(deltaXZ > 0.2
                    && data.playerInfo.lastVelocity.isPassed(4)
                    && data.playerInfo.lastTeleportTimer.isPassed(1)) {
                if(++sprintBuffer > 3) {
                    vl++;
                    flag("type=SPRINT a=%v.1 b=%v", angle, sprintBuffer);
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
                    flag("type=INVALID sprint=%v a=%v.1 b=%v", angle, invalidBuffer);
                }
            } else invalidBuffer = 0;

            debug("dxz=%v.5 base=%v.5", deltaXZ, base);
        }
    }

    @Event
    public void onTeleport(PlayerTeleportEvent event) {
        event.getPlayer().setSprinting(false);
    }

}
