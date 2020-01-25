package dev.brighten.anticheat.check.impl.movement.general;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.types.enums.WrappedEnumParticle;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.Materials;
import cc.funkemunky.api.utils.Tuple;
import cc.funkemunky.api.utils.world.BlockData;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.Helper;
import dev.brighten.anticheat.utils.RayCollision;
import dev.brighten.api.check.CheckType;
import dev.brighten.db.utils.Pair;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@CheckInfo(name = "Phase", description = "Firefly phase chekkkkk",  checkType = CheckType.GENERAL, punishVL = 20,
        developer = true)
public class Phase extends Check {

    private static boolean debug = true;
    private KLocation setBack;

    @Packet
    void check(WrappedInFlyingPacket packet) {
        if (!packet.isPos()
                || data.playerInfo.serverPos) return;

        if (data.playerInfo.deltaY > -0.0981 && data.playerInfo.deltaY < -0.0979) return;


        SimpleCollisionBox currentHitbox = Helper.getMovementHitbox(data.getPlayer());
        SimpleCollisionBox newHitbox = Helper.getMovementHitbox(data.getPlayer(), packet.getX(), packet.getY(), packet.getZ());
        currentHitbox.expand(-0.0625); newHitbox.expand(-0.0625); // reduce falseflag chances
        SimpleCollisionBox wrapped = Helper.wrap(currentHitbox, newHitbox);

        List<Block> all = Helper.getBlocks(data.blockInfo.handler, wrapped);
        List<Block> currentBlocks = Helper.blockCollisions(all, currentHitbox);
        List<Block> newBlocks = Helper.blockCollisions(all, newHitbox);

        for (Block b : newBlocks) {
            if (!currentBlocks.contains(b)) {
                if (Materials.checkFlag(b.getType(), Materials.SOLID) && !Materials.checkFlag(b.getType(), Materials.STAIRS)) {
                    vl++;
                    flag("t=%1", b.getType().name());
                    if (debug)
                        BlockData.getData(b.getType()).getBox(b, data.playerVersion)
                                .draw(WrappedEnumParticle.FLAME, Collections.singleton(data.getPlayer()));
                    data.getPlayer().teleport(data.playerInfo.from.toLocation(data.getPlayer().getWorld()));
                    return;
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
                if (RayCollision.intersect(ray, box.toBoundingBox(), pair)) {
                    if (pair.one <= dist) {
                        if (!data.playerInfo.wasOnSlime && dist >= 1) {
                            vl++;
                            flag("d=%1", dist);
                        }
                        data.getPlayer().teleport(data.playerInfo.from.toLocation(data.getPlayer().getWorld()));
                    }
                }
            }
        }
    }
}
