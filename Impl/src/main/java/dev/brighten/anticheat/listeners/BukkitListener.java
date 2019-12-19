package dev.brighten.anticheat.listeners;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.types.enums.WrappedEnumParticle;
import cc.funkemunky.api.utils.BoundingBox;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.world.BlockData;
import cc.funkemunky.api.utils.world.types.ComplexCollisionBox;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.Kauri;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

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

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInteract(PlayerInteractEvent event) {
        if(event.getClickedBlock() == null || !event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;

        if(event.getItem() != null && event.getItem().isSimilar(MAGIC_WAND)) {
            Block block = event.getClickedBlock();

            val box = BlockData.getData(block.getType()).getBox(block, ProtocolVersion.getGameVersion());

            box.draw(WrappedEnumParticle.FLAME, Collections.singleton(event.getPlayer()));
            event.setCancelled(true);
        }
    }
}
