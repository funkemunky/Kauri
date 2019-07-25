package cc.funkemunky.anticheat.impl.listeners;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.lunar.LunarClientAPI;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.ConfigSetting;
import cc.funkemunky.api.utils.Init;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

@Init
public class LunarListeners implements Listener {

    private boolean testMode = true;

    @ConfigSetting(path = "lunar", name = "kickOnJoin")
    private boolean kickOnJoin = true;

    private String kickMessage = "&cYou have been kicked for using Lunar Client.\n&7This server does not allow Lunar Client since\n&7it provides extra reach hits, which is\n&7an unfair advantage.";

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if(!event.getPlayer().hasPermission("kauri.admin") && !event.getPlayer().hasPermission("kauri.lunarKick.bypass")) {
            new BukkitRunnable() {
                public void run() {
                    if(LunarClientAPI.getInstance().getUserManager().getPlayerData(event.getPlayer().getUniqueId()).isLunarClient()) {
                        event.getPlayer().kickPlayer(Color.translate(kickMessage));
                    }
                }
            }.runTaskLater(Kauri.getInstance(), 40L);
        }
    }
}
