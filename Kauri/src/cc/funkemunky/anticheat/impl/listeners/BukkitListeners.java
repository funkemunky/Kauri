package cc.funkemunky.anticheat.impl.listeners;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.CheckSettings;
import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.anticheat.api.utils.BukkitEvents;
import cc.funkemunky.anticheat.api.utils.CustomLocation;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.tinyprotocol.packet.types.WrappedEnumParticle;
import cc.funkemunky.api.utils.BoundingBox;
import cc.funkemunky.api.utils.Color;
import org.bukkit.Achievement;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerAchievementAwardedEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@cc.funkemunky.api.utils.Init
public class BukkitListeners implements Listener {

    @EventHandler
    public void onEvent(PlayerMoveEvent event) {
        PlayerData data = Kauri.getInstance().getDataManager().getPlayerData(event.getPlayer().getUniqueId());

        if (data != null) {
            callChecks(data, event);

            if (data.getCancelType().equals(CancelType.MOTION)) {
                if (data.getSetbackLocation() != null) {
                    event.getPlayer().teleport(data.getSetbackLocation());
                } else {
                    event.getPlayer().teleport(MiscUtils.findGround(event.getTo().getWorld(), new CustomLocation(event.getTo())).toLocation(event.getTo().getWorld()));
                }
                data.setCancelType(CancelType.NONE);
            } else if (data.getMovementProcessor().isServerOnGround() && data.getLastFlag().hasPassed()) {
                Location setback = data.getMovementProcessor().getTo().toLocation(event.getPlayer().getWorld()).clone();

                setback.setY(setback.getBlockY());
                data.setSetbackLocation(setback);
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
            if (event.getBlockPlaced() != null && event.getBlockPlaced().getType().isSolid()) {
                data.getLastBlockPlace().reset();
            }
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

            if (event.getItem() != null && event.getItem().isSimilar(cc.funkemunky.api.utils.MiscUtils.createItem(Material.BLAZE_ROD, 1, Color.Gold + "Magic Box Wand")) && event.getClickedBlock() != null) {
                List<BoundingBox> boxes = Atlas.getInstance().getBlockBoxManager().getBlockBox().getSpecificBox(event.getClickedBlock().getLocation());

                boxes.forEach(box -> cc.funkemunky.api.utils.MiscUtils.createParticlesForBoundingBox(event.getPlayer(), box, WrappedEnumParticle.FLAME, 0.1f));
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

                if (data.getCancelType() == CancelType.PROJECTILE) {
                    event.setCancelled(true);
                    data.setCancelType(CancelType.NONE);
                }
            }
        }
    }

    @EventHandler
    public void onEvent(PlayerAchievementAwardedEvent event) {
        if (event.getAchievement().equals(Achievement.OPEN_INVENTORY)) {
            PlayerData data = Kauri.getInstance().getDataManager().getPlayerData(event.getPlayer().getUniqueId());

            if (data != null) {
                data.getActionProcessor().setOpenInventory(true);
                event.setCancelled(true);
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
        if ((!CheckSettings.bypassEnabled || !data.getPlayer().hasPermission(CheckSettings.bypassPermission)) && !Kauri.getInstance().getCheckManager().isBypassing(data.getUuid())) {
            Atlas.getInstance().getThreadPool().execute(() -> {
                data.getBukkitChecks().getOrDefault(event.getClass(), new ArrayList<>()).stream()
                        .filter(check -> check.isEnabled() && check.getClass().isAnnotationPresent(BukkitEvents.class) && Arrays.asList(check.getClass().getAnnotation(BukkitEvents.class).events()).contains(event.getClass()))
                        .forEach(check -> check.onBukkitEvent(event));
            });
        }
    }
}
