package dev.brighten.anticheat.listeners;

import cc.funkemunky.api.utils.Init;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.data.ObjectData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

@Init
public class BukkitCheckListeners implements Listener {

    @EventHandler(priority = EventPriority.LOW)
    public void onInteract(PlayerInteractEvent event) {
        ObjectData data = Kauri.INSTANCE.dataManager.getData(event.getPlayer());

        if(data != null) {
            data.checkManager.runEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onMove(PlayerMoveEvent event) {
        ObjectData data = Kauri.INSTANCE.dataManager.getData(event.getPlayer());

        if(data != null) {
            data.checkManager.runEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBookEdit(PlayerEditBookEvent event) {
        ObjectData data = Kauri.INSTANCE.dataManager.getData(event.getPlayer());

        if(data != null) {
            data.checkManager.runEvent(event);
        }
    }
}
