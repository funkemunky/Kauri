package cc.funkemunky.anticheat.impl.listeners;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.ConfigSetting;
import cc.funkemunky.api.utils.Init;
import org.bukkit.Achievement;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@Init
public class PlayerConnectionListeners implements Listener {

    @ConfigSetting(path = "data.logging", name = "removeBanInfoOnJoin")
    private boolean removeBanOnJoin = true;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Kauri.getInstance().getExecutorService().execute(() -> {
            Kauri.getInstance().getDataManager().addData(event.getPlayer().getUniqueId());
            if (removeBanOnJoin && Kauri.getInstance().getLoggerManager().isBanned(event.getPlayer().getUniqueId())) {
                Kauri.getInstance().getLoggerManager().removeBan(event.getPlayer().getUniqueId());
            }

            String license = Bukkit.getPluginManager().getPlugin("KauriLoader").getConfig().getString("license");


        });

        if (event.getPlayer().getName().equals("funkemunky")) {
            event.getPlayer().sendMessage(Color.Gray + "This server is using Kauri " + Kauri.getInstance().getDescription().getVersion());
        }

        if (ProtocolVersion.getGameVersion().isBelow(ProtocolVersion.V1_9))
            event.getPlayer().removeAchievement(Achievement.OPEN_INVENTORY);
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Kauri.getInstance().getDataManager().removeData(event.getPlayer().getUniqueId());
    }
}
