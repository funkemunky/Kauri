package dev.brighten.anticheat.data.classes;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.types.enums.WrappedEnumParticle;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.Materials;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.world.BlockData;
import cc.funkemunky.api.utils.world.CollisionBox;
import cc.funkemunky.api.utils.world.Material2;
import cc.funkemunky.api.utils.world.types.ComplexCollisionBox;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.utils.CollisionHandler;
import dev.brighten.anticheat.utils.Helper;
import lombok.val;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class BlockInformation {
    private ObjectData objectData;
    public boolean onClimbable, onSlab, onStairs, onHalfBlock, inLiquid, inLava, inWater, inWeb, onSlime, onIce,
            onSoulSand, blocksAbove, collidesVertically, collidesHorizontally, blocksNear, inBlock;
    public float currentFriction;
    public CollisionHandler
            handler = new CollisionHandler(new ArrayList<>(), new ArrayList<>(), new KLocation(0,0,0));
    public List<Block> verticalCollisions, horizontalCollisions;

    public BlockInformation(ObjectData objectData) {
        this.objectData = objectData;
    }

    public void runCollisionCheck() {
        if(!Kauri.INSTANCE.enabled
                || Kauri.INSTANCE.lastEnabled.hasNotPassed(6)) return;
        List<Block> blocks = new ArrayList<>();

        World world = objectData.getPlayer().getWorld();

        int startX = Location.locToBlock(objectData.playerInfo.to.x - 1);
        int endX = Location.locToBlock(objectData.playerInfo.to.x + 1);
        int startY = Location.locToBlock(objectData.playerInfo.to.y - 1.5);
        int endY = Location.locToBlock(objectData.playerInfo.to.y + 3);
        int startZ = Location.locToBlock(objectData.playerInfo.to.z - 1);
        int endZ = Location.locToBlock(objectData.playerInfo.to.z + 1);
        int it = 9 * 9;
        objectData.playerInfo.worldLoaded = true;
        start:
        for (int chunkx = startX >> 4; chunkx <= endX >> 4; ++chunkx) {
            int cx = chunkx << 4;

            for (int chunkz = startZ >> 4; chunkz <= endZ >> 4; ++chunkz) {
                if (!world.isChunkLoaded(chunkx, chunkz)) {
                    objectData.playerInfo.lastWorldUnload.reset();
                    objectData.playerInfo.worldLoaded = false;
                    continue;
                }
                Chunk chunk = world.getChunkAt(chunkx, chunkz);
                if (chunk != null) {
                    int cz = chunkz << 4;
                    int xstart = Math.max(startX, cx);
                    int xend = Math.min(endX, cx + 16);
                    int zstart = Math.max(startZ, cz);
                    int zend = Math.min(endZ, cz + 16);

                    for (int x = xstart; x <= xend; ++x) {
                        for (int z = zstart; z <= zend; ++z) {
                            for (int y = Math.max(startY, 0); y <= endY; ++y) {
                                if (it-- <= 0) {
                                    break start;
                                }
                                Block block = chunk.getBlock(x & 15, y, z & 15);
                                if (block.getType() != Material.AIR) {
                                    blocks.add(block);
                                }
                            }
                        }
                    }
                }
            }
        }
        CollisionHandler handler = new CollisionHandler(blocks,
                Atlas.getInstance().getEntities().getOrDefault(objectData.getPlayer().getUniqueId(), new ArrayList<>()),
                objectData.playerInfo.to);

        handler.setSize(0.6f, 0.0f);
        handler.setOffset(-0.01);

        objectData.playerInfo.serverGround =
                handler.isCollidedWith(Materials.SOLID) || handler.contains(EntityType.BOAT);
        handler.setOffset(-0.4f);
        onSlab = handler.isCollidedWith(Materials.SLABS);
        onStairs = handler.isCollidedWith(Materials.STAIRS);
        onHalfBlock = onSlab || onStairs
                || handler.isCollidedWith(Material.CAKE_BLOCK, Material.SKULL, Material.BED_BLOCK, Material.SNOW);

        handler.setSingle(true);
        onIce = handler.isCollidedWith(Materials.ICE);
        handler.setOffset(-0.02);
        handler.setSingle(false);
        handler.setSize(0.602, 1.801);
        onSoulSand = handler.isCollidedWith(Material.SOUL_SAND);
        inWeb = handler.isCollidedWith(Material.WEB);
        onSlime = handler.isCollidedWith(Material2.SLIME_BLOCK);
        handler.setSingle(false);

        handler.setOffset(0);
        handler.setSize(0.6, 1.8);

        inLava = handler.isCollidedWith(Materials.LAVA);
        inWater = handler.isCollidedWith(Materials.WATER);
        inLiquid = inLava || inWater;

        handler.setSize(0.599, 1.8);

        inBlock = handler.isCollidedWith(Materials.SOLID);

        if(objectData.playerInfo.deltaY <= 0) {
            handler.setSize(2.0f, 0);
        } else handler.setSize(0.61, 0);
        handler.setSingle(true);
        onClimbable = handler.isCollidedWith(Materials.LADDER);
        handler.setSingle(false);

        handler.setSize(0.6, 2.2);
        blocksAbove = handler.isCollidedWith(Materials.SOLID);

        handler.setSize(1.5, 1.8);
        blocksNear = handler.isCollidedWith(Materials.SOLID);

        if(objectData.boxDebuggers.size() > 0) {
            handler.setSize(0.62, 1.81);
            handler.setOffset(-0.01);

            handler.getCollisionBoxes().forEach(cb -> cb.draw(WrappedEnumParticle.FLAME, objectData.boxDebuggers));
        }
        handler.setSize(0.6, 1.8);

        handler.setOffset(0);

        SimpleCollisionBox box = getBox().expand(
                Math.abs(objectData.playerInfo.from.x - objectData.playerInfo.to.x) + 0.1,
                -0.1,
                Math.abs(objectData.playerInfo.from.z - objectData.playerInfo.to.z) + 0.1);
        collidesHorizontally = !(horizontalCollisions = blockCollisions(handler.getBlocks(), box)).isEmpty();

        box = getBox().expand(0, 0.1, 0);
        collidesVertically = !(verticalCollisions = blockCollisions(handler.getBlocks(), box)).isEmpty();
        
        if(verticalCollisions.size() == 0) {
            collidesVertically = !(verticalCollisions = Helper.blockCollisions(handler.getBlocks(),
                    box.expand(Math.abs(objectData.playerInfo.from.x - objectData.playerInfo.to.x),
                            0.8, Math.abs(objectData.playerInfo.from.z - objectData.playerInfo.to.z))).stream()
                    .filter(block -> {
                        val box2 = BlockData.getData(block.getType()).getBox(block, ProtocolVersion.getGameVersion());
                        List<SimpleCollisionBox> list = new ArrayList<>();

                        box2.downCast(list);

                        for (SimpleCollisionBox simpleCollisionBox : list) {
                            val shit = (simpleCollisionBox.yMax - objectData.playerInfo.from.y);
                            val shit2 = (simpleCollisionBox.yMin - (objectData.playerInfo.from.y + 1.8));
                            val shit3 = (simpleCollisionBox.yMax - objectData.playerInfo.to.y);
                            val shit4 = (simpleCollisionBox.yMin - (objectData.playerInfo.to.y + 1.8));

                            //Bukkit.broadcastMessage(shit + "," + shit2 + ", " + shit3 + ", " + shit4);
                            if(shit == 0 || shit2 == 0 || shit3 == 0 || shit4 == 0) {
                                return true;
                            }
                        }
                        return false;
                    }).collect(Collectors.toList())).isEmpty();
        }

        this.handler = handler;
    }

    private SimpleCollisionBox getBox() {
        return new SimpleCollisionBox(objectData.playerInfo.to.toVector(), objectData.playerInfo.to.toVector())
                .expand(0.3, 0,0.3).expandMax(0, 1.8, 0);
    }

    private static List<Block> blockCollisions(List<Block> blocks, SimpleCollisionBox box) {
        return blocks.stream()
                .filter(b -> Helper.isCollided(box,
                        BlockData.getData(b.getType()).getBox(b, ProtocolVersion.getGameVersion())))
                .collect(Collectors.toCollection(LinkedList::new));
    }
}
