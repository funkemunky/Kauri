package cc.funkemunky.anticheat.api.lunar.listener;

import cc.funkemunky.api.events.AtlasListener;
import cc.funkemunky.api.events.Listen;
import cc.funkemunky.anticheat.api.lunar.LunarClientAPI;
import cc.funkemunky.anticheat.api.lunar.event.AuthenticateEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class WaypointListener implements AtlasListener, Listener {

    @Listen
    public void onAuthenticate(AuthenticateEvent event) {
        LunarClientAPI.getInstance().getWaypointManager().reloadWaypoints(event.getPlayer(), true);
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        LunarClientAPI.getInstance().getWaypointManager().reloadWaypoints(event.getPlayer(), true);
    }
}
