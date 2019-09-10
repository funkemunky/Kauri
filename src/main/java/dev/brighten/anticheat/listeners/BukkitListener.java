package dev.brighten.anticheat.listeners;

import cc.funkemunky.api.utils.Init;
import dev.brighten.anticheat.Kauri;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@Init
public class BukkitListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Kauri.INSTANCE.dataManager.dataMap.remove(event.getPlayer().getUniqueId());
        Kauri.INSTANCE.executor.execute(() ->  Kauri.INSTANCE.dataManager.createData(event.getPlayer()));
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Kauri.INSTANCE.dataManager.dataMap.remove(event.getPlayer().getUniqueId());
    }
}
