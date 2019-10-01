package dev.brighten.anticheat.listeners;

import cc.funkemunky.api.bungee.events.BungeeReceiveEvent;
import cc.funkemunky.api.events.AtlasListener;
import cc.funkemunky.api.events.Listen;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Config;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

@Init
public class BungeeListener implements AtlasListener {

    //uuid, info, checkName, vl
    @Listen
    public void onBungee(BungeeReceiveEvent event) {
        if(!Config.bungeeAlerts || event.objects.length != 4) return;
        UUID uuid = (UUID)event.objects[0];
        String checkName = (String)event.objects[1];
        float vl = (float)event.objects[2];
        String info = (String)event.objects[3];

        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);

        String alert = Color.translate("&8[&6K&8] &f" + player.getName() + " &7flagged &f" + checkName + " &8(&e" + info + "&8) &8[&c" + vl + "&8]");
        Kauri.INSTANCE.dataManager.hasAlerts.forEach(data -> data.getPlayer().sendMessage(alert));
    }
}
