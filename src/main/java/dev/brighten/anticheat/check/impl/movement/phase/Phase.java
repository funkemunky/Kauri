package dev.brighten.anticheat.check.impl.movement.phase;

import cc.funkemunky.api.reflection.MinecraftReflection;
import cc.funkemunky.api.utils.*;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.CheckType;
import dev.brighten.anticheat.check.api.Event;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.List;
import java.util.stream.Collectors;

@CheckInfo(name = "Phase", description = "Prevents players from glitching through blocks",
        checkType = CheckType.GLITCH, enabled = false, executable = false, punishVL = 50)
public class Phase extends Check {

    private TickTimer lastOpenDoor = new TickTimer(5);
    private EvictingList<Location> nonColliding = new EvictingList<>(5);
    private TickTimer lastFlag = new TickTimer(5);

    @Event
    public void onFlying(PlayerMoveEvent event) {
        if(event.getTo().distance(event.getFrom()) > 0) {
            if(Kauri.INSTANCE.enabled
                    && !data.playerInfo.serverCanFly
                    && !data.playerInfo.inCreative) {

                boolean flagged = false;
                BoundingBox box = new BoundingBox(event.getFrom().toVector(), event.getTo().toVector())
                        .grow(0.3f,0,0.3f)
                        .add(0,0f,0,0,1.8f,0);

                List<BoundingBox> boxes = ReflectionsUtil
                        .getCollidingBlocks(data.getPlayer(), MinecraftReflection.toAABB(box))
                        .parallelStream()
                        .map(MinecraftReflection::fromAABB)
                        .filter(blockBox -> {
                            Block block = blockBox.getMinimum()
                                    .toLocation(data.getPlayer().getWorld())
                                    .getBlock();

                            return !BlockUtils.isStair(block)
                                    && !BlockUtils.isSlab(block)
                                    && blockBox.maxY - blockBox.minY != 0.5f
                                    && BlockUtils.isSolid(block)
                                    && blockBox.intersectsWithBox(box);
                        })
                        .collect(Collectors.toList());

                if (boxes.size() > 0) {
                    if(lastOpenDoor.hasPassed(8)) {
                        setBack();
                        if (vl++ > 10) {
                            flag("phased");
                        }
                        flagged = true;
                    }
                    lastFlag.reset();
                } else {
                    if(!data.playerInfo.collidesHorizontally) nonColliding.add(event.getFrom());
                    vl-= vl > 0 ? 0.05 : 0;
                }

                if(!flagged) vl-= vl > 0 ? 0.02 : 0;

                debug("size=" + boxes.size() + " nonColliding=" + nonColliding.size());
            }
        }
    }

    @Event
    public void onInteract(PlayerInteractEvent event) {
        if(!event.isCancelled()
                && event.getAction().equals(Action.RIGHT_CLICK_BLOCK)
                && event.getClickedBlock() != null
                && event.getClickedBlock().getType().isSolid()
                && (BlockUtils.isDoor(event.getClickedBlock())
                || BlockUtils.isTrapDoor(event.getClickedBlock())
                || BlockUtils.isFenceGate(event.getClickedBlock()))) {
            lastOpenDoor.reset();
        }
    }

    private void setBack() {
        RunUtils.task(() -> data.getPlayer()
                .teleport(nonColliding.get(nonColliding.size() - 1)), Kauri.INSTANCE);
    }
}
