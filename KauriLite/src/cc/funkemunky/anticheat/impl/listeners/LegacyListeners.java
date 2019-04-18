package cc.funkemunky.anticheat.impl.listeners;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.data.PlayerData;
import org.bukkit.Achievement;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAchievementAwardedEvent;

public class LegacyListeners implements Listener {

    @EventHandler
    public void onEvent(PlayerAchievementAwardedEvent event) {
        if (event.getAchievement().equals(Achievement.OPEN_INVENTORY)) {
            PlayerData data = Kauri.getInstance().getDataManager().getPlayerData(event.getPlayer().getUniqueId());

            if (data != null) {
                data.getActionProcessor().setOpenInventory(true);
                event.setCancelled(true);
            }
        }
    }
}
