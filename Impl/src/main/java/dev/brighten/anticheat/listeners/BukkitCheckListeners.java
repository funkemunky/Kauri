package dev.brighten.anticheat.listeners;

import cc.funkemunky.api.utils.Init;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.data.ObjectData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.*;

@Init
public class BukkitCheckListeners implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event) {
        ObjectData data = Kauri.INSTANCE.dataManager.getData(event.getPlayer());

        if(data != null) {
            data.playerInfo.breakingBlock = event.getAction().equals(Action.LEFT_CLICK_BLOCK);
            data.checkManager.runEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMove(PlayerMoveEvent event) {
        ObjectData data = Kauri.INSTANCE.dataManager.getData(event.getPlayer());

        if(data != null) {
            data.checkManager.runEvent(event);
        }
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onEvent(PlayerTeleportEvent event) {
        ObjectData data = Kauri.INSTANCE.dataManager.getData(event.getPlayer());

        if(data != null) {
            data.moveProcessor.moveTo(event.getTo());
            if(data.checkManager != null)
            data.checkManager.runEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEvent(PlayerRespawnEvent event) {
        ObjectData data = Kauri.INSTANCE.dataManager.getData(event.getPlayer());

        if(data != null) {
            data.moveProcessor.moveTo(event.getRespawnLocation());
            if(data.checkManager != null)
            data.checkManager.runEvent(event);
        }
    }

    @EventHandler
    public void onBlock(BlockPlaceEvent event) {
        ObjectData data = Kauri.INSTANCE.dataManager.getData(event.getPlayer());

        if(data != null) {
            data.checkManager.runEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBookEdit(PlayerEditBookEvent event) {
        ObjectData data = Kauri.INSTANCE.dataManager.getData(event.getPlayer());

        if(data != null) {
            data.checkManager.runEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEvent(SignChangeEvent event) {
        ObjectData data = Kauri.INSTANCE.dataManager.getData(event.getPlayer());

        if(data != null) {
            data.checkManager.runEvent(event);
        }
    }
}
