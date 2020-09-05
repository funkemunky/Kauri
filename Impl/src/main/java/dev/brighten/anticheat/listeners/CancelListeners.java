package dev.brighten.anticheat.listeners;

import cc.funkemunky.api.utils.BlockUtils;
import cc.funkemunky.api.utils.Init;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.CancelType;
import dev.brighten.anticheat.data.ObjectData;
import lombok.val;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

@Init
public class CancelListeners implements Listener {

    /** Cancels for MOVEMENT **/
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEvent(PlayerMoveEvent event) {
        ObjectData data = Kauri.INSTANCE.dataManager.getData(event.getPlayer());

        if(data != null && data.typesToCancel.size() > 0) {
            for (CancelType cancelType : data.typesToCancel) {
                if(!cancelType.equals(CancelType.MOVEMENT)) continue;

                val ground = BlockUtils.findGround(event.getFrom().getWorld(), event.getFrom()).clone();

                ground.setYaw(event.getFrom().getYaw());
                ground.setPitch(event.getFrom().getPitch());

                event.getPlayer().teleport(ground.add(0,0.01,0));
                data.typesToCancel.remove(cancelType);
                return;
            }
        }
    }

    /** Cancels for ATTACK **/
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEvent(EntityDamageByEntityEvent event) {
        if(!(event.getDamager() instanceof Player)) return;

        ObjectData data = Kauri.INSTANCE.dataManager.getData((Player)event.getDamager());

        if(data != null && data.typesToCancel.size() > 0) {
            for (CancelType cancelType : data.typesToCancel) {
                if(!cancelType.equals(CancelType.ATTACK)) continue;

                event.setCancelled(true);
                data.typesToCancel.remove(cancelType);
                break;
            }
        }
    }

    /** Cancels for PLACE **/
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEvent(BlockPlaceEvent event) {
        ObjectData data = Kauri.INSTANCE.dataManager.getData(event.getPlayer());

        if(data != null && data.typesToCancel.size() > 0) {
            for (CancelType cancelType : data.typesToCancel) {
                if(!cancelType.equals(CancelType.PLACE)) continue;

                event.setCancelled(true);
                data.typesToCancel.remove(cancelType);
                break;
            }
        }
    }

    /** Cancels for BREAK **/
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEvent(BlockBreakEvent event) {
        ObjectData data = Kauri.INSTANCE.dataManager.getData(event.getPlayer());

        if(data != null && data.typesToCancel.size() > 0) {
            for (CancelType cancelType : data.typesToCancel) {
                if(!cancelType.equals(CancelType.BREAK)) continue;

                event.setCancelled(true);
                data.typesToCancel.remove(cancelType);
                break;
            }
        }
    }

    /** Cancels for Interact **/
    //Done for block and sword interact stuff.
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEvent(PlayerInteractEvent event) {
        ObjectData data = Kauri.INSTANCE.dataManager.getData(event.getPlayer());

        if(data != null && data.typesToCancel.size() > 0) {
            for (CancelType cancelType : data.typesToCancel) {
                if(!cancelType.equals(CancelType.INTERACT)) continue;

                event.setCancelled(true);
                data.typesToCancel.remove(cancelType);
                break;
            }
        }
    }

    /** Cancels for Interact **/
    //Done for interacting with entities like NPC or players.
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEvent(EntityInteractEvent event) {
        if(!(event.getEntity() instanceof Player)) return;

        ObjectData data = Kauri.INSTANCE.dataManager.getData((Player)event.getEntity());

        if(data != null && data.typesToCancel.size() > 0) {
            for (CancelType cancelType : data.typesToCancel) {
                if(!cancelType.equals(CancelType.INTERACT)) continue;

                event.setCancelled(true);
                data.typesToCancel.remove(cancelType);
                break;
            }
        }
    }
}
