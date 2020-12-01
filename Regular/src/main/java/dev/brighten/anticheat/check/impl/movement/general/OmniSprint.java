package dev.brighten.anticheat.check.impl.movement.general;

import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.anticheat.utils.MiscUtils;
import dev.brighten.api.check.CheckType;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

@CheckInfo(name = "OmniSprint", description = "Checks for sprinting in illegal directions.",
        checkType = CheckType.GENERAL, vlToFlag = 15, punishVL = 50, developer = true)
@Cancellable
public class OmniSprint extends Check {

    private int buffer;

    @Event
    public void onPlayerMove(PlayerMoveEvent event) {
        double angle = MiscUtils.getAngle(event.getTo().clone(), event.getFrom().clone());
        double deltaYaw = MathUtils.getYawDifference(event.getTo(), event.getFrom());
        double deltaXZ = MathUtils.getHorizontalDistance(event.getTo(), event.getFrom());

        if(event.getPlayer().isSprinting() && Math.abs(angle) < 95
                && data.playerInfo.serverGround
                && deltaXZ > 0.2
                && data.playerInfo.lastVelocity.isPassed(20)) {
            if(++buffer > 3) {
                vl++;
                flag("angle=%v.1 t=%v", angle, buffer);
            }
            debug("angle=%v.3", angle);
        } else buffer = 0;
    }

    @Event
    public void onTeleport(PlayerTeleportEvent event) {
        event.getPlayer().setSprinting(false);
    }

}
