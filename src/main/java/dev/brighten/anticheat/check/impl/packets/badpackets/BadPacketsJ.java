package dev.brighten.anticheat.check.impl.packets.badpackets;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.*;
import org.bukkit.Location;
import org.bukkit.entity.Player;
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
        float dirYaw = (float)(Math.atan2(dir.getX(), dir.getZ()) * 180 / Math.PI) - 90f;
    }

}
