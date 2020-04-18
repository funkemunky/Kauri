package dev.brighten.anticheat.processing;

import cc.funkemunky.api.utils.Init;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.data.ObjectData;
import gg.manny.lunar.event.PlayerAuthenticateEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

@Init(requirePlugins = "LunarClientAPI")
public class LunarClientProcessor implements Listener  {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAuth(PlayerAuthenticateEvent event) {
        ObjectData data = Kauri.INSTANCE.dataManager.getData(event.getPlayer());

        if(data == null) {
            Kauri.INSTANCE.dataManager.createData(event.getPlayer());
            data = Kauri.INSTANCE.dataManager.getData(event.getPlayer());
        }
        data.usingLunar = true;
    }
}
