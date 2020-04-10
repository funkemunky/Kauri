package dev.brighten.anticheat.utils.lunar.listener;

import cc.funkemunky.api.utils.Init;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.utils.lunar.LunarClientAPI;
import dev.brighten.anticheat.utils.lunar.user.User;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.IOException;

@Init
public class PlayerListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        User data = new User(event.getPlayer().getUniqueId(), event.getPlayer().getName());
        LunarClientAPI.getInstance().getUserManager().setPlayerData(event.getPlayer().getUniqueId(), data);

        Kauri.INSTANCE.getServer().getScheduler().runTaskLater(Kauri.INSTANCE, () -> {
            User user = LunarClientAPI.getInstance().getUserManager().getPlayerData(event.getPlayer());
            if (user != null) {
                ObjectData odata = Kauri.INSTANCE.dataManager.getData(event.getPlayer());

                if(odata != null) {
                    odata.usingLunar = true;
                }
            }
        }, 40L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        LunarClientAPI.getInstance().getUserManager().removePlayerData(event.getPlayer().getUniqueId());
    }
}
