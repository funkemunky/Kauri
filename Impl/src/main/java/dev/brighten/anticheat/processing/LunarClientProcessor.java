package dev.brighten.anticheat.processing;

import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.RunUtils;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.data.ObjectData;
import gg.manny.lunar.LunarClientAPI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

@Init(requirePlugins = "LunarClientAPI")
public class LunarClientProcessor implements Listener  {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        RunUtils.taskLaterAsync(() -> {
            ObjectData data = Kauri.INSTANCE.dataManager.getData(event.getPlayer());

            data.usingLunar = LunarClientAPI.getInstance().getPlayers().contains(data.uuid);
        }, Kauri.INSTANCE, 60L);
    }
}
