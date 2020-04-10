package dev.brighten.anticheat.utils.lunar.listener;

import dev.brighten.anticheat.utils.lunar.LunarClientAPI;
import dev.brighten.anticheat.utils.lunar.module.border.Border;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.IOException;

public class BorderListener implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        for (Border border : LunarClientAPI.getInstance().getBorderManager().getBorderList()){
            try {
                border.disable(event.getPlayer());
            } catch (IOException e) {
                //ignore
            }
        }
    }

    @EventHandler
    public void onKick(PlayerKickEvent event){
        for (Border border : LunarClientAPI.getInstance().getBorderManager().getBorderList()){
            try {
                border.disable(event.getPlayer());
            } catch (IOException e) {
                //ignore
            }
        }
    }
}
