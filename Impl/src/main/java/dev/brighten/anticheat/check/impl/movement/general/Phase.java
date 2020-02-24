package dev.brighten.anticheat.check.impl.movement.general;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.types.enums.WrappedEnumParticle;
import cc.funkemunky.api.utils.*;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import cc.funkemunky.api.utils.world.BlockData;
import cc.funkemunky.api.utils.world.types.RayCollision;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Event;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.Helper;
import dev.brighten.api.check.CheckType;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CheckInfo(name = "Phase", description = "a phase check made by the one and only LukeK.",
        checkType = CheckType.GENERAL, punishVL = 20,
        developer = true, enabled = false)
public class Phase extends Check {

    private static boolean debug = false;
    private EvictingList<KLocation> previousLocs = new EvictingList<>(12);
    private TickTimer lastOpen = new TickTimer(7);

    private int setbackTicks = 0;
    private KLocation setback;

    private static List<Material> negative = Stream.of(XMaterial.LADDER, XMaterial.VINE)
            .map(XMaterial::parseMaterial)
            .collect(Collectors.toList());

    @Event
    public void onEvent(PlayerInteractEvent event) {
        if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)
                && event.getClickedBlock() != null
                && !event.isCancelled()
                && event.getClickedBlock().getType().name().contains("DOOR")) lastOpen.reset();
    }

    @Packet
    public void check(WrappedInFlyingPacket packet, long timeStamp) {
        if (!packet.isPos()) return;

        if ((data.playerInfo.deltaY > -0.0981 && data.playerInfo.deltaY < -0.0979 && data.playerInfo.deltaXZ < 0.5)
                || lastOpen.hasNotPassed(14)) return;


        if(timeStamp - data.playerInfo.lastServerPos > 10L) {
            SimpleCollisionBox currentHitbox = Helper.getMovementHitbox(data.getPlayer());
            SimpleCollisionBox newHitbox = Helper.getMovementHitbox(data.getPlayer(), packet.getX(), packet.getY(), packet.getZ());
            currentHitbox.expand(-0.0625); newHitbox.expand(-0.0625); // reduce falseflag chances
            SimpleCollisionBox wrapped = Helper.wrap(currentHitbox, newHitbox);

            List<Block> all = Helper.getBlocks(data.blockInfo.handler, wrapped);
            List<Block> currentBlocks = Helper.blockCollisions(all, currentHitbox);
            List<Block> newBlocks = Helper.blockCollisions(all, newHitbox);

            boolean flagged = false;
            for (Block b : newBlocks) {
                if (!currentBlocks.contains(b)) {
                    if (Materials.checkFlag(b.getType(), Materials.SOLID)
                            && !negative.contains(b.getType())
                            && !Materials.checkFlag(b.getType(), Materials.STAIRS)) {
                        vl++;
                        flag("t=%1", b.getType().name());
                        if (debug)
                            BlockData.getData(b.getType()).getBox(b, data.playerVersion)
                                    .draw(WrappedEnumParticle.FLAME, Collections.singleton(data.getPlayer()));
                        if(setbackTicks <= 0) {
                            if(data.playerInfo.deltaXZ > 1.2) {
                                if (previousLocs.size() == 0)
                                    setback = data.playerInfo.from;
                                else setback = previousLocs.get(Math.max(0, previousLocs.size() - 3));
                            } else setback = data.playerInfo.from;
                        }
                        setbackTicks=2;
                        flagged = true;
                    }
                }
            }

            if (!currentHitbox.isCollided(newHitbox)) { // moved too far, must check between
                List<SimpleCollisionBox> downcasted = new LinkedList<>();
                Helper.toCollisions(all.stream().filter(b -> Materials.checkFlag(b.getType(), Materials.SOLID))
                        .collect(Collectors.toList())).forEach((b) -> b.downCast(downcasted));
                Vector newPos = new Vector(packet.getX(), packet.getY()+data.getPlayer().getEyeHeight(), packet.getZ());
                // so this is something stupid, Just some dumb ray tracing to check if anything was between,
                // its a bad patch for phasing and i should replace it later, but for now...
                Vector oldPos = data.getPlayer().getEyeLocation().toVector();
                double dist = newPos.distance(oldPos);
                Vector rayDir = newPos.subtract(oldPos);
                RayCollision ray = new RayCollision(oldPos, rayDir);
                Tuple<Double, Double> pair = new Tuple<>(0d, 0d);
                for (SimpleCollisionBox box : downcasted) {
                    if (RayCollision.intersect(ray, box, pair)) {
                        if (pair.one <= dist) {
                            if (!data.playerInfo.wasOnSlime && dist >= 1) {
                                vl++;
                                flag("d=%1", dist);
                            }
                            if(setbackTicks <= 0) {
                                if(data.playerInfo.deltaXZ > 1.2) {
                                    if (previousLocs.size() == 0)
                                        setback = data.playerInfo.from;
                                    else setback = previousLocs.get(Math.max(0, previousLocs.size() - 3));
                                } else setback = previousLocs.size() > 0
                                        ? previousLocs.get(Math.max(0, previousLocs.size() - 2))
                                        : data.playerInfo.from;
                            }
                            setbackTicks=2;
                            flagged = true;
                        }
                    }
                }
            }
            if(!flagged) {
                data.blockInfo.handler.setSize(0.84f, 1.799f);
                data.blockInfo.handler.setOffset(-0.001);

                if(!data.blockInfo.handler.isCollidedWith(Materials.SOLID)) {
                    previousLocs.add(data.playerInfo.from);
                }
                data.blockInfo.handler.setSize(0.6f, 1.8f);
                data.blockInfo.handler.setOffset(0);
            }
        }

        setback();
    }

    private void setback() {
        if(setbackTicks-- <= 0) return;

        data.getPlayer().teleport(setback.toLocation(data.getPlayer().getWorld()));
    }
}