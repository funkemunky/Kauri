package dev.brighten.anticheat.check.impl.world.place;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockPlacePacket;
import cc.funkemunky.api.utils.BlockUtils;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.world.BlockData;
import cc.funkemunky.api.utils.world.types.RayCollision;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.anticheat.utils.MiscUtils;
import dev.brighten.api.check.CheckType;
import lombok.val;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Cancellable(cancelType = CancelType.PLACE)
@CheckInfo(name = "Block (Place)", description = "Checks to see if the player ever looked at the block placed.",
        checkType = CheckType.BLOCK, developer = true, punishVL = 20)
public class BlockPlace extends Check {

    private static double maxDistance = 4.5;
    @Packet
    public void onBlockPlace(WrappedInBlockPlacePacket packet) {
        if(!data.playerInfo.worldLoaded
                || data.playerInfo.creative
                || packet.getItemStack() == null
                || !packet.getItemStack().getType().isBlock()
                || packet.getItemStack().getType().equals(Material.AIR)
                || data.lagInfo.lastPacketDrop.hasNotPassed(10)) return;

        val face = new Vector(
                packet.getFace().getAdjacentX(), packet.getFace().getAdjacentY(), packet.getFace().getAdjacentZ());
        val placeLoc = new Location(packet.getPlayer().getWorld(),
                packet.getPosition().getX(), packet.getPosition().getY(), packet.getPosition().getZ());

        Block block = BlockUtils.getBlock(placeLoc);
        if(block != null && BlockUtils.isSolid(block)) {
            val pastLoc = data.pastLocation.getPreviousRange(175).stream()
                    .peek(loc -> loc.y+=data.playerInfo.sneaking ? 1.54 : 1.62)
                    .map(loc -> new RayCollision(loc.toVector(), MiscUtils.getDirection(loc)))
                    .collect(Collectors.toList());

            if(pastLoc.size() == 0 || data.lagInfo.lastPacketDrop.hasNotPassed(3)) return;

            List<SimpleCollisionBox> boxes = new ArrayList<>();

            BlockData
                    .getData(block.getType()).getBox(block, ProtocolVersion.getGameVersion())
                    .downCast(boxes);

            boxes.forEach(box -> box.expand(0.2, 0.2, 0.2));

            List<Double> distances = new ArrayList<>();

            for (RayCollision ray : pastLoc) {
                //ray.draw(WrappedEnumParticle.FLAME, Collections.singleton(data.getPlayer()));
                for (SimpleCollisionBox box : boxes) {
                    val point = ray.collisionPoint(box);
                    //box.draw(WrappedEnumParticle.FLAME, Collections.singleton(data.getPlayer()));

                    if(point != null) {
                        distances.add(ray.getOrigin().toVector().distance(point));
                    }
                }
            }

            double distance = -1;
            if(distances.size() == 0) {
                vl++;
                flag("t:[no collision]; distance=-1 collisions=0");
            } else {
                distance = distances.stream().min(Comparator.comparingDouble(val -> val)).orElse(-1D);

                if(distance > maxDistance) {
                    vl++;
                    flag("t:[distance] distance=%1 collisions=%2",
                            MathUtils.round(distance, 2), distances.size());
                }
            }

            debug("distance=%1 collisions=%2 boxes%3 rays%4", distance, distances.size(), boxes.size(), pastLoc.size());
        }
    }
}
