package cc.funkemunky.anticheat.api.lunar.listener;

import cc.funkemunky.anticheat.api.lunar.LunarClientAPI;
import cc.funkemunky.anticheat.api.lunar.user.User;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRegisterChannelEvent;
import org.bukkit.event.player.PlayerUnregisterChannelEvent;

import java.io.IOException;

public class ClientListener implements Listener {

    @EventHandler
    public void onRegisterChannel(PlayerRegisterChannelEvent event){
        if (event.getChannel().equals("Lunar-Client")) {
            try {
                LunarClientAPI.getInstance().performEmote(event.getPlayer(), 5, false);
                LunarClientAPI.getInstance().performEmote(event.getPlayer(), -1, false);
            } catch (IOException e) {
                //ignore
            }
        }
    }

    @EventHandler
    public void onUnregisterChannel(PlayerUnregisterChannelEvent event) {
        User user = LunarClientAPI.getInstance().getUserManager().getPlayerData(event.getPlayer());
        if (event.getChannel().equals("Lunar-Client")) {
            user.setLunarClient(false);
        }
    }
}
