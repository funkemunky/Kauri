package dev.brighten.anticheat.check.impl.movement.phase;

import cc.funkemunky.api.reflection.MinecraftReflection;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.types.WrappedEnumParticle;
import cc.funkemunky.api.utils.*;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import cc.funkemunky.carbon.utils.Pair;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.anticheat.utils.RayCollision;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@CheckInfo(name = "Phase", description = "Prevents players from glitching through blocks",
        checkType = CheckType.GLITCH, enabled = true, developer = true, executable = false, punishVL = 50)
public class Phase extends Check {

    private TickTimer lastOpenDoor = new TickTimer(5);
    private EvictingList<Location> nonColliding = new EvictingList<>(8);

    @Event
    public void on(PlayerMoveEvent e) {
        if(e.getTo().distance(e.getFrom()) > 0
                && data.playerInfo.deltaXZ != 0 || data.playerInfo.deltaY != 0
                && Kauri.INSTANCE.enabled
                && !data.playerInfo.serverCanFly
                && !data.playerInfo.inCreative) {

            BoundingBox currentBox = data.box.shrink(0.06f,0,0.05f);
            BoundingBox fromBox = new BoundingBox(e.getFrom().toVector(), e.getFrom().toVector())
                    .grow(0.24f,0,0.24f)
                    .add(0,0,0,
                            0, (float)data.getPlayer().getEyeHeight(), 0);
            BoundingBox total = new BoundingBox(fromBox, currentBox);

            List<BoundingBox> colliding = MinecraftReflection
                    .getCollidingBoxes(null, data.getPlayer().getWorld(), total);

            List<BoundingBox> phasedBlocks = colliding.stream()
                    .filter(box -> !currentBox.collides(box) && fromBox.collides(box))
                    .collect(Collectors.toList());

            if(data.playerInfo.worldLoaded) {
                Optional<Block> optional = phasedBlocks
                        .stream()
                        .map(box -> box.getMinimum().midpoint(box.getMaximum())
                                .toLocation(data.getPlayer().getWorld()).getBlock())
                        .filter(BlockUtils::isSolid).findFirst();
                if(optional.isPresent()) {
                    setBack(e.getFrom());
                    if(vl++ > 4) {
                        flag("block=" + optional.get().getType().name().toLowerCase());
                    }
                } else if(currentBox.collides(fromBox) && !data.blockInfo.blocksNear) {
                    nonColliding.add(e.getFrom());
                    vl-= vl > 0 ? 0.025f : 0;
                } else {
                    vl-= vl > 0 ? 0.025f : 0;
                }

                //Checking too see if the player clipped through blocks.
                if(!currentBox.collides(fromBox)) {
                    Vector newLoc = e.getTo().toVector()
                            .add(new Vector(0, (float)data.getPlayer().getEyeHeight(), 0));
                    Vector from = e.getFrom().toVector()
                            .add(new Vector(0, (float)data.getPlayer().getEyeHeight(), 0));

                    Vector dir = newLoc.clone().subtract(from.clone());
                    RayCollision collision = new RayCollision(from, dir);

                    Tuple<Double, Double> tuple = new Tuple<>(0.,0.);
                    double distance = from.distance(newLoc);

                    List<BoundingBox> allSolids = colliding.stream().filter(box ->
                            BlockUtils.isSolid(box.getMinimum().midpoint(box.getMaximum())
                                    .toLocation(data.getPlayer().getWorld()).getBlock()))
                            .collect(Collectors.toList());

                    for(BoundingBox box : allSolids) {
                        if(RayCollision.intersect(collision, box, tuple)) {
                            if(tuple.one <= distance) {
                                flag("clipped=" + distance);
                                setBack(e.getFrom());
                            }
                        }
                    }
                }
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

    private void setBack(Location from) {
        if(nonColliding.size() > 0) {
            RunUtils.task(() -> {
                if(nonColliding.size() > 0) {
                    data.getPlayer().teleport(nonColliding.get(nonColliding.size() - 1));
                } else data.getPlayer().teleport(from);
            }, Kauri.INSTANCE);
        }
    }
}
