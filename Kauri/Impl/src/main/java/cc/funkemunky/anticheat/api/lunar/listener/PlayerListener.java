package cc.funkemunky.anticheat.api.lunar.listener;

import cc.funkemunky.anticheat.api.lunar.LunarClientAPI;
import cc.funkemunky.anticheat.api.lunar.user.User;
import cc.funkemunky.api.utils.Init;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@Init
public class PlayerListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        User data = new User(event.getPlayer().getUniqueId(), event.getPlayer().getName());
        LunarClientAPI.getInstance().getUserManager().setPlayerData(event.getPlayer().getUniqueId(), data);

    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        LunarClientAPI.getInstance().getUserManager().removePlayerData(event.getPlayer().getUniqueId());
    }
}
