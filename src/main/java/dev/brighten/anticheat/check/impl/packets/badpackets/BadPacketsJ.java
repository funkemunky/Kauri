package dev.brighten.anticheat.check.impl.packets.badpackets;

import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.CheckType;
import dev.brighten.anticheat.check.api.Event;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

@CheckInfo(name = "BadPackets (J)", description = "Checks for omni sprint.", checkType = CheckType.BADPACKETS)
public class BadPacketsJ extends Check {

    @Event
    public void onPacket(PlayerMoveEvent event) {
        Location from = event.getFrom().clone();
        Location to = event.getTo().clone();

        double eyeHeight = event.getPlayer().getEyeHeight();

        Vector dir = to.clone().add(0, eyeHeight, 0).toVector().subtract(from.clone().toVector());
        float dirYaw = MathUtils.yawTo180F((float)(Math.atan2(dir.getX(), dir.getZ()) * 180 / Math.PI) - 90f);
        float yaw = MathUtils.yawTo180F(to.getYaw());

        float delta = MathUtils.getDelta(Math.abs(dirYaw), Math.abs(yaw));

        debug("yaw=" + yaw + " dirYaw=" + dirYaw + " delta=" + delta);
    }

}
