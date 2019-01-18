package cc.funkemunky.anticheat.impl.listeners;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.anticheat.api.utils.BukkitEvents;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Arrays;

public class BukkitListeners implements Listener {

    @EventHandler
    public void onEvent(PlayerMoveEvent event) {
        PlayerData data = Kauri.getInstance().getDataManager().getPlayerData(event.getPlayer().getUniqueId());

        if (data != null && data.getLastMovementCancel().hasPassed(1)) {
            callChecks(data, event);

            if (data.getCancelType().equals(CancelType.MOTION)) {
                event.getPlayer().teleport(data.getSetbackLocation());
                data.setCancelType(CancelType.NONE);
            } else if (data.getMovementProcessor().isServerOnGround()) {
                data.setSetbackLocation(event.getTo());
            }
        }
    }

    @EventHandler
    public void onEvent(BlockBreakEvent event) {
        PlayerData data = Kauri.getInstance().getDataManager().getPlayerData(event.getPlayer().getUniqueId());

        if (data != null) {
            callChecks(data, event);

            if (data.getCancelType().equals(CancelType.BREAK)) {
                event.setCancelled(true);
                data.setCancelType(CancelType.NONE);
            }
        }
    }

    @EventHandler
    public void onEvent(BlockPlaceEvent event) {
        PlayerData data = Kauri.getInstance().getDataManager().getPlayerData(event.getPlayer().getUniqueId());

        if (data != null) {
            callChecks(data, event);

            if (data.getCancelType().equals(CancelType.PLACE)) {
                event.setCancelled(true);
                data.setCancelType(CancelType.NONE);
            }
        }
    }

    @EventHandler
    public void onEvent(PlayerInteractEvent event) {
        PlayerData data = Kauri.getInstance().getDataManager().getPlayerData(event.getPlayer().getUniqueId());

        if (data != null) {
            callChecks(data, event);

            if (data.getCancelType().equals(CancelType.INTERACT)) {
                event.setCancelled(true);
                data.setCancelType(CancelType.NONE);
            }
        }
    }

    @EventHandler
    public void onEvent(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() != null && event.getEntity().getShooter() instanceof Player) {
            Player player = (Player) event.getEntity().getShooter();
            PlayerData data = Kauri.getInstance().getDataManager().getPlayerData(player.getUniqueId());

            if (data != null) {
                callChecks(data, event);
            }
        }
    }

    @EventHandler
    public void onEvent(EntityRegainHealthEvent event) {
        if (event.getEntity() instanceof Player) {
            PlayerData data = Kauri.getInstance().getDataManager().getPlayerData(event.getEntity().getUniqueId());

            if (data != null) {
                callChecks(data, event);

                if (data.getCancelType().equals(CancelType.HEALTH)) {
                    event.setCancelled(true);

                    data.setCancelType(CancelType.NONE);
                }
            }
        }
    }

    private void callChecks(PlayerData data, Event event) {
        data.getChecks().stream()
                .filter(check -> check.isEnabled() && check.getClass().isAnnotationPresent(BukkitEvents.class) && Arrays.asList(check.getClass().getAnnotation(BukkitEvents.class).events()).contains(event.getClass()))
                .forEach(check -> check.onBukkitEvent(event));
    }
}
