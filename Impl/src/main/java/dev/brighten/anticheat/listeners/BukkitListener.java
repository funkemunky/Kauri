package dev.brighten.anticheat.listeners;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.types.enums.WrappedEnumParticle;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.world.BlockData;
import cc.funkemunky.api.utils.world.CollisionBox;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.data.ObjectData;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;

@Init
public class BukkitListener implements Listener {

    public static ItemStack MAGIC_WAND = MiscUtils.createItem(Material.BLAZE_ROD, 1, "&6Magic Wand");

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent event) {
        Kauri.INSTANCE.dataManager.dataMap.remove(event.getPlayer().getUniqueId());
        Kauri.INSTANCE.executor.execute(() ->  Kauri.INSTANCE.dataManager.createData(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onLeave(PlayerQuitEvent event) {
        //Removing if the player has debug access so there aren't any null objects left to cause problems later.
        if(event.getPlayer().hasPermission("kauri.debug"))
            ObjectData.debugBoxes(false, event.getPlayer());
        ObjectData data = Kauri.INSTANCE.dataManager.getData(event.getPlayer());
        if(data != null) data.onLogout();
        Kauri.INSTANCE.dataManager.dataMap.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInteract(PlayerInteractEvent event) {
        if(event.getClickedBlock() == null || !event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;

        if(event.getItem() != null && event.getItem().isSimilar(MAGIC_WAND)) {
            BlockData data = BlockData.getData(event.getClickedBlock().getType());
            CollisionBox box = data.getBox(event.getClickedBlock(), ProtocolVersion.getGameVersion());

            box.draw(WrappedEnumParticle.FLAME, Collections.singleton(event.getPlayer()));
            event.setCancelled(true);
        }
    }
}
