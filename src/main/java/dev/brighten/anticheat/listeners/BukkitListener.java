package dev.brighten.anticheat.listeners;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.reflection.MinecraftReflection;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MiscUtils;
import dev.brighten.anticheat.Kauri;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

@Init
public class BukkitListener implements Listener {

    public static ItemStack MAGIC_WAND = MiscUtils.createItem(Material.BLAZE_ROD, 1, "&6Magic Wand");

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Kauri.INSTANCE.dataManager.dataMap.remove(event.getPlayer().getUniqueId());
        Kauri.INSTANCE.executor.execute(() ->  Kauri.INSTANCE.dataManager.createData(event.getPlayer()));
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Kauri.INSTANCE.dataManager.dataMap.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if(event.getClickedBlock() == null || !event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;


        if(event.getItem() != null && event.getItem().isSimilar(MAGIC_WAND)) {
            MinecraftReflection.getCollidingBoxes()
            event.setCancelled(true);
        }
    }
}
