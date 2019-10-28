package dev.brighten.anticheat.check.impl.movement.phase;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.utils.BlockUtils;
import cc.funkemunky.api.utils.BoundingBox;
import cc.funkemunky.api.utils.RunUtils;
import cc.funkemunky.api.utils.TickTimer;
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
import org.bukkit.material.Gate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@CheckInfo(name = "Phase", description = "Prevents players from glitching through blocks",
        checkType = CheckType.GLITCH, enabled = false, executable = false, punishVL = 50)
public class Phase extends Check {

    private List<BoundingBox> lastboxes = new ArrayList<>();
    private TickTimer lastOpenDoor = new TickTimer(5);
    private EvictingList<Location> nonColliding = new EvictingList<>(5);
    private TickTimer lastFlag = new TickTimer(5);

    @Event
    public void onFlying(PlayerMoveEvent event) {
        if(event.getTo().distance(event.getFrom()) > 0) {
            if(Kauri.INSTANCE.enabled
                    && !data.playerInfo.serverCanFly
                    && !data.playerInfo.inCreative) {
                if(lastboxes.size() > 0) {
                    debug("From block");
                }

                boolean flagged = false;
                BoundingBox box = new BoundingBox(event.getTo().toVector(), event.getFrom().toVector())
                        .grow(0.25f,0,0.25f)
                        .add(0,0.25f,0,0,1.72f,0);

                List<BoundingBox> boxes = Atlas.getInstance().getBlockBoxManager().getBlockBox()
                        .getCollidingBoxes(data.getPlayer().getWorld(), box)
                        .parallelStream()
                        .filter(blockBox -> {
                            Block block = blockBox.getMinimum()
                                    .toLocation(data.getPlayer().getWorld())
                                    .getBlock();

                            if(block.getType().getNewData(block.getData()) instanceof Gate) {
                                Gate gate = (Gate) block.getType().getNewData(block.getData());

                                return blockBox.intersectsWithBox(box)
                                        && !gate.isOpen();
                            }
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
                } else if(!data.playerInfo.collidesHorizontally) {
                    nonColliding.add(event.getTo());
                    vl-= vl > 0 ? 0.05 : 0;
                }

                if(!flagged) {
                    lastboxes = boxes.stream()
                            .filter(bbox -> bbox.intersectsWithBox(box.shrink(0.2f,0,0.2f)))
                            .collect(Collectors.toList());
                    vl-= vl > 0 ? 0.02 : 0;
                } else lastboxes.clear();
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
                .teleport(nonColliding.get(0)), Kauri.INSTANCE);
    }
}
