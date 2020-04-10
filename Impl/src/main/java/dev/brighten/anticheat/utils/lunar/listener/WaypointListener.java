package dev.brighten.anticheat.utils.lunar.listener;

import dev.brighten.anticheat.utils.lunar.LunarClientAPI;
import dev.brighten.anticheat.utils.lunar.event.impl.AuthenticateEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class WaypointListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAuthenticate(AuthenticateEvent event) {
        LunarClientAPI.getInstance().getWaypointManager().reloadWaypoints(event.getPlayer(), true);
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        LunarClientAPI.getInstance().getWaypointManager().reloadWaypoints(event.getPlayer(), true);
    }
}
