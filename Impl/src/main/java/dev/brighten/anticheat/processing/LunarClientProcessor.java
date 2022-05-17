package dev.brighten.anticheat.processing;

import cc.funkemunky.api.utils.Init;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.data.ObjectData;
import com.lunarclient.bukkitapi.LunarClientAPI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

@Init(requirePlugins = "LunarClient-API")
public class LunarClientProcessor implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        ObjectData data = Kauri.INSTANCE.dataManager.getData(event.getPlayer());

        if (data == null) {
            data = Kauri.INSTANCE.dataManager.getData(event.getPlayer());
        }

        data.usingLunar = LunarClientAPI
            .getInstance()
            .isRunningLunarClient(event.getPlayer());
    }
}
