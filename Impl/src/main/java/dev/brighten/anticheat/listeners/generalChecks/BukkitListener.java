package dev.brighten.anticheat.listeners.generalChecks;

import cc.funkemunky.api.reflections.impl.MinecraftReflection;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.types.enums.WrappedEnumParticle;
import cc.funkemunky.api.utils.*;
import cc.funkemunky.api.utils.world.BlockData;
import cc.funkemunky.api.utils.world.CollisionBox;
import cc.funkemunky.api.utils.world.state.BlockStateManager;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.utils.ThreadHandler;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Init
public class BukkitListener implements Listener {

    public static ItemStack MAGIC_WAND = MiscUtils.createItem(XMaterial.BLAZE_ROD.parseMaterial(),
            1, "&6Magic Wand");

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        ThreadHandler.addPlayer(event.getPlayer());
        Kauri.INSTANCE.dataManager.createData(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLeave(PlayerQuitEvent event) {
        ThreadHandler.removePlayer(event.getPlayer());
        //Removing if the player has debug access so there aren't any null objects left to cause problems later.
        if(event.getPlayer().hasPermission("kauri.debug"))
            ObjectData.debugBoxes(false, event.getPlayer());
        ObjectData data = Kauri.INSTANCE.dataManager.getData(event.getPlayer());
        if(data != null) data.unregister();
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if(event.getClickedBlock() == null || !event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;

        if(event.getItem() != null && event.getItem().isSimilar(MAGIC_WAND)) {
            BlockData data = BlockData.getData(event.getClickedBlock().getType());

            CollisionBox box = data.getBox(event.getClickedBlock(), ProtocolVersion.getGameVersion());

            Bukkit.dispatchCommand(event.getPlayer(), "kauri block "
                    + event.getClickedBlock().getType().name());

            List<SimpleCollisionBox> downcasted = new ArrayList<>();

            box.downCast(downcasted);

            for (SimpleCollisionBox sbox : downcasted) {
                val max = sbox.max().subtract(event.getClickedBlock().getLocation().toVector());
                val min = sbox.min().subtract(event.getClickedBlock().getLocation().toVector());

                Vector subbed = max.subtract(min);

                event.getPlayer().sendMessage(sbox.min().toString());
                event.getPlayer().sendMessage(sbox.max().toString());
                event.getPlayer().sendMessage("x=" + subbed.getX() + " y=" + subbed.getY() + " z=" + subbed.getZ());
            }

            if(BlockUtils.isDoor(event.getClickedBlock())) {
                int direction = (int) BlockStateManager.getInterface("facing", event.getClickedBlock());
                boolean open = (boolean) BlockStateManager.getInterface("open", event.getClickedBlock());
                boolean hinge = (boolean) BlockStateManager.getInterface("hinge", event.getClickedBlock());
                boolean topOrBelow = (boolean) BlockStateManager.getInterface("top", event.getClickedBlock());

                event.getPlayer().sendMessage(String.format("dir=%s open=%s hinge=%s top=%s",
                        direction, open, hinge, topOrBelow));
            }

            box.draw(WrappedEnumParticle.FLAME, Collections.singleton(event.getPlayer()));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event) {
        ObjectData data = Kauri.INSTANCE.dataManager.getData(event.getPlayer());

        if(data == null || event.getBlockPlaced() == null) return;

        if(event.isCancelled()) {
            data.ghostBlocks.put(event.getBlockPlaced().getLocation(),
                    BlockData.getData(event.getBlockPlaced().getType())
                            .getBox(event.getBlockPlaced(), data.playerVersion));
        } else data.ghostBlocks.remove(event.getBlockPlaced().getLocation());
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        if(event.getPlayer().getItemInHand() == null
                || !event.getPlayer().getItemInHand().isSimilar(MAGIC_WAND)) return;
        if(MiscUtils.entityDimensions.containsKey(event.getRightClicked().getType())) {
            Vector dimension = MiscUtils.entityDimensions.get(event.getRightClicked().getType());

            SimpleCollisionBox box = new SimpleCollisionBox(event.getRightClicked().getLocation().toVector(), event.getRightClicked().getLocation().toVector())
                    .expand(dimension.getX(), 0, dimension.getZ())
                    .expandMax(0, dimension.getY(), 0);

            box.draw(WrappedEnumParticle.FLAME, Collections.singleton(event.getPlayer()));
            event.getPlayer().sendMessage(Color.Gold + Color.Bold
                    + event.getRightClicked().getType() + ": " + Color.White);
            event.getPlayer().sendMessage(boxToString(box));
        } else {
            SimpleCollisionBox box = MinecraftReflection
                    .fromAABB(ReflectionsUtil
                            .getBoundingBox(event.getRightClicked()))
                    .toCollisionBox();
            box.draw(WrappedEnumParticle.FLAME, Collections.singleton(event.getPlayer()));
            event.getPlayer().sendMessage(Color.Gold + Color.Bold
                    + event.getRightClicked().getType() + ": " + Color.White);
            event.getPlayer().sendMessage(boxToString(box));
        }
        event.setCancelled(true);
    }

    private static String vectorString = "{%1$.2f, %2$.2f, %3$.2f}";
    private static String boxToString(CollisionBox box) {
        if(box instanceof SimpleCollisionBox) {
            SimpleCollisionBox sbox = (SimpleCollisionBox) box;
            return "SimpleCollisionBox[" + vectorToString(sbox.toBoundingBox().getMinimum())
                    + ", " + vectorToString(sbox.toBoundingBox().getMaximum()) + "]";
        } else {
            List<SimpleCollisionBox> downCasted = new ArrayList<>();

            box.downCast(downCasted);

            return "ComplexBox[" + downCasted.stream()
                    .map(sbox -> "SimpleCollisionBox[" + vectorToString(sbox.toBoundingBox().getMinimum())
                    + ", " + vectorToString(sbox.toBoundingBox().getMaximum()) + "]")
                    .collect(Collectors.joining(", ")) + "]";
        }
    }

    private static String vectorToString(Vector vector) {
        return String.format(vectorString, vector.getX(), vector.getY(), vector.getZ());
    }
}
