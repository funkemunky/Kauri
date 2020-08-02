package dev.brighten.anticheat.check.impl.movement.general;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.types.enums.WrappedEnumParticle;
import cc.funkemunky.api.utils.*;
import cc.funkemunky.api.utils.world.BlockData;
import cc.funkemunky.api.utils.world.CollisionBox;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.api.check.CheckType;
import dev.brighten.db.utils.Pair;
import lombok.val;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@CheckInfo(name = "Phase", description = "Ensures players cannot move through blocks.",
        checkType = CheckType.EXPLOIT, cancellable = true, executable = false, developer = true)
public class Phase extends Check {

    private KLocation fromWhereShitAintBad = null;
    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long now) {
        if(now - data.creation < 1200L || now - data.playerInfo.lastRespawn < 150L || data.playerInfo.serverPos)
            return;

        KLocation to = data.playerInfo.to, from = data.playerInfo.from;

        if(fromWhereShitAintBad == null) fromWhereShitAintBad = from;

        SimpleCollisionBox box = new SimpleCollisionBox(from.toVector(), to.toVector());

        box.expand(0.3, 0, 0.3);
        box.expandMax(0, 1.8, 0);
        box.expand(-0.025);

        box.sort();

        int minX = MathUtils.floor(box.xMin), minY = MathUtils.floor(box.yMin), minZ = MathUtils.floor(box.zMin);
        int maxX = MathUtils.floor(box.xMax + 1), maxY = MathUtils.floor(box.yMax + 1), maxZ = MathUtils.floor(box.zMax + 1);

        List<Pair<Block, CollisionBox>> boxes = new ArrayList<>();
        for(int x = minX ; x < maxX ; x++) {
            for(int y = minY; y < maxY ; y++) {
                for(int z = minZ ; z < maxZ ; z++) {
                    Block block = BlockUtils.getBlock(new Location(packet.getPlayer().getWorld(), x, y, z));

                    if(block == null || block.isEmpty() || !Materials
                            .checkFlag(block.getType(), Materials.SOLID)) continue;

                    val blockBox = BlockData.getData(block.getType()).getBox(block, data.playerVersion);

                    if(box.isCollided(blockBox)) {
                        boxes.add(new Pair<>(block, blockBox));
                    }
                }
            }
        }

        //box.draw(WrappedEnumParticle.CRIT_MAGIC, Collections.singleton(packet.getPlayer()));

        if(boxes.size() > 0) {
            debug("collided=%v blocks=%v", boxes.size(), boxes.stream()
                    .map(pair -> pair.key.getType().name()).collect(Collectors.joining(", ")));

            RunUtils.task(() -> packet.getPlayer().teleport(from.toLocation(packet.getPlayer().getWorld())));
        } else if(!data.blockInfo.collidesHorizontally) {
            fromWhereShitAintBad = data.playerInfo.to;
        }
    }
}
