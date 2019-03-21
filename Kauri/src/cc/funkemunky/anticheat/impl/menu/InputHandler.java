package cc.funkemunky.anticheat.impl.menu;

import cc.funkemunky.api.utils.Init;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

@Init
public class InputHandler implements Listener {

    @EventHandler
    public void onEvent(SignChangeEvent event) {

    }

    public String openInput(Player player) {
        player.openInventory(Bukkit.createInventory(null, InventoryType.))
    }
}
