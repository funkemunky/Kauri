package dev.brighten.anticheat.listeners;

import cc.funkemunky.api.bungee.events.BungeeReceiveEvent;
import cc.funkemunky.api.events.AtlasListener;
import cc.funkemunky.api.events.Listen;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Config;
import dev.brighten.anticheat.utils.timer.Timer;
import dev.brighten.anticheat.utils.timer.impl.AtlasTimer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Init
public class BungeeListener implements AtlasListener, Listener {

    //uuid, info, checkName, vl
    public Map<CheckEntry, Timer> lastAlertsMap = new HashMap<>();

    @Listen
    public void onBungee(BungeeReceiveEvent event) {
        if(!Config.bungeeAlerts || event.objects.length != 4) return;
        final UUID uuid = (UUID)event.objects[0];
        final String checkName = (String)event.objects[1];
        final CheckEntry entry = new CheckEntry(uuid, checkName);
        final Timer lastAlert;
        if(!lastAlertsMap.containsKey(entry)) {
            lastAlert = new AtlasTimer(MathUtils.millisToTicks(Config.alertsDelay));
            lastAlertsMap.put(entry, lastAlert);
        } else {
            lastAlert = lastAlertsMap.get(entry);
        }

        if(lastAlert.isPassed(MathUtils.millisToTicks(Config.alertsDelay))) {
            final float vl = (float)event.objects[2];
            final String info = (String)event.objects[3];

            final OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);

            final String alert = Color.translate("&8[&6K&8] &f" + player.getName()
                    + " &7flagged &f" + checkName
                    + " &8(&e" + info + "&8) &8[&c" + vl + "&8]");

            for (int hashcode : Kauri.INSTANCE.dataManager.hasAlerts.toArray(new int[0])) {
                Kauri.INSTANCE.dataManager.dataMap.get(hashcode).getPlayer().sendMessage(alert);
            }
            lastAlert.reset();
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        lastAlertsMap.keySet()
                .stream()
                .filter(entry -> entry.uuid.equals(event.getPlayer().getUniqueId()))
                .forEach(lastAlertsMap::remove);
    }
}
