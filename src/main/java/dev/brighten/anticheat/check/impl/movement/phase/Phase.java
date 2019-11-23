package dev.brighten.anticheat.check.impl.movement.phase;

import cc.funkemunky.api.reflection.MinecraftReflection;
import cc.funkemunky.api.utils.BlockUtils;
import cc.funkemunky.api.utils.BoundingBox;
import cc.funkemunky.api.utils.Tuple;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.CheckType;
import dev.brighten.anticheat.check.api.Event;
import dev.brighten.anticheat.utils.RayCollision;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@CheckInfo(name = "Phase", description = "Prevents players from glitching through blocks. Stolen from Firefly (thanks Luke)",
        checkType = CheckType.GLITCH, developer = true, executable = false, punishVL = 50)
public class Phase extends Check {

    @Event
    public void on(PlayerMoveEvent e) {
        Location eFrom = e.getFrom().clone();
        if(e.getTo().clone().distance(eFrom) > 0
                && data.playerInfo.deltaXZ != 0 || data.playerInfo.deltaY != 0
                && Kauri.INSTANCE.enabled
                && !e.getPlayer().getAllowFlight()
                && !data.playerInfo.inCreative) {

            if(data.playerInfo.worldLoaded) {
                BoundingBox currentBox = data.box.shrink(0.06f,0,0.05f);
                BoundingBox fromBox = new BoundingBox(eFrom.toVector(), eFrom.toVector())
                        .grow(0.24f,0,0.24f)
                        .add(0,0,0,
                                0, (float)data.getPlayer().getEyeHeight(), 0).shrink(0, 0.1f,0);
                BoundingBox total = new BoundingBox(fromBox, currentBox);

                List<BoundingBox> colliding = MinecraftReflection
                        .getCollidingBoxes(null, data.getPlayer().getWorld(), total);

                List<BoundingBox> phasedBlocks = colliding.stream()
                        .filter(box -> !currentBox.collides(box) && fromBox.collides(box))
                        .collect(Collectors.toList());
                Optional<Block> optional = phasedBlocks
                        .stream()
                        .map(box -> box.getMinimum().midpoint(box.getMaximum())
                                .toLocation(data.getPlayer().getWorld()).getBlock())
                        .filter(BlockUtils::isSolid).findFirst();
                if(optional.isPresent()) {
                    data.getPlayer().teleport(eFrom);
                    if(vl++ > 4) {
                        flag("block=" + optional.get().getType().name().toLowerCase());
                    }
                } else {
                    vl-= vl > 0 ? 0.025f : 0;
                }

                //Checking too see if the player clipped through blocks.
                Vector newLoc = e.getTo().toVector()
                        .add(new Vector(0, (float)data.getPlayer().getEyeHeight(), 0));
                Vector from = eFrom.toVector()
                        .add(new Vector(0, (float)data.getPlayer().getEyeHeight(), 0));

                Vector dir = newLoc.clone().subtract(from.clone());
                RayCollision collision = new RayCollision(from, dir);

                Tuple<Double, Double> tuple = new Tuple<>(0.,0.);
                double distance = from.distance(newLoc);

                List<BoundingBox> allSolids = colliding.stream().filter(box -> {
                    Block block = box.getMinimum().midpoint(box.getMaximum())
                            .toLocation(data.getPlayer().getWorld()).getBlock();
                    return BlockUtils.isSolid(block) && !BlockUtils.isStair(block) && !BlockUtils.isSlab(block);
                })
                        .collect(Collectors.toList());

                for(BoundingBox box : allSolids) {
                    box.minY = (float)e.getFrom().getY();
                    box.maxY = (float)(e.getTo().getY() + data.getPlayer().getEyeHeight());
                    if(RayCollision.intersect(collision, box, tuple)) {
                        if(tuple.one <= distance) {
                            if(vl++ > 4) {
                                flag("clipped=" + distance);
                            }
                            data.getPlayer().teleport(eFrom);
                            break;
                        }
                        debug("distance=" + distance + ", " + tuple.one);
                    }
                }
            }
        }
    }
}
