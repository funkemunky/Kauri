package dev.brighten.anticheat.listeners;

import cc.funkemunky.api.utils.Init;
import dev.brighten.anticheat.Kauri;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

@Init
public class BukkitListener {

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Kauri.INSTANCE.dataManager.dataMap.remove(event.getPlayer().getUniqueId());
    }
}
