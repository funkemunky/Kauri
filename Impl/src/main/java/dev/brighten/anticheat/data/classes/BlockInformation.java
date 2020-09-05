package dev.brighten.anticheat.data.classes;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.types.enums.WrappedEnumParticle;
import cc.funkemunky.api.utils.BlockUtils;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.Materials;
import cc.funkemunky.api.utils.XMaterial;
import cc.funkemunky.api.utils.world.BlockData;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.processing.EntityProcessor;
import dev.brighten.anticheat.utils.CacheList;
import dev.brighten.anticheat.utils.CacheMap;
import dev.brighten.anticheat.utils.CollisionHandler;
import dev.brighten.anticheat.utils.Helper;
import lombok.val;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BlockInformation {
    private ObjectData objectData;
    public boolean onClimbable, onSlab, onStairs, onHalfBlock, inLiquid, inLava, inWater, inWeb, onSlime, onIce,
            onSoulSand, blocksAbove, collidesVertically, collidesHorizontally, blocksNear, inBlock, miscNear;
    public float currentFriction, fromFriction;
    public CollisionHandler
            handler = new CollisionHandler(new ArrayList<>(), new ArrayList<>(), new KLocation(0,0,0));
    public List<Block> verticalCollisions, horizontalCollisions;
    public List<SimpleCollisionBox> aboveCollisions = new ArrayList<>(), belowCollisions = new ArrayList<>();
    public final List<Block> blocks = Collections.synchronizedList(new ArrayList<>());

    public BlockInformation(ObjectData objectData) {
        this.objectData = objectData;
    }

    public void runCollisionCheck() {
        if(!Kauri.INSTANCE.enabled
                || Kauri.INSTANCE.lastEnabled.hasNotPassed(6)) return;
        synchronized (blocks) {
            blocks.clear();
        }

        double dy = Math.abs(objectData.playerInfo.deltaY);
        double dh = objectData.playerInfo.deltaXZ;

        if(dy > 3) dy = 3;
        if(dh > 3) dh = 3;

        int startX = Location.locToBlock(objectData.playerInfo.to.x - 2 - dh);
        int endX = Location.locToBlock(objectData.playerInfo.to.x + 2 + dh);
        int startY = Location.locToBlock(objectData.playerInfo.to.y - 2 - dy);
        int endY = Location.locToBlock(objectData.playerInfo.to.y + 3 + dy);
        int startZ = Location.locToBlock(objectData.playerInfo.to.z - 2 - dh);
        int endZ = Location.locToBlock(objectData.playerInfo.to.z + 2 + dh);

        objectData.playerInfo.worldLoaded = true;
        synchronized (blocks) {
            for(int x = startX ; x < endX ; x++) {
                for(int y = startY ; y < endY ; y++) {
                    for(int z = startZ ; z < endZ ; z++) {
                        Location loc = new Location(objectData.getPlayer().getWorld(), x, y, z);

                        if(loc.getWorld().isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)) {
                            blocks.add(loc.getBlock());
                        } else objectData.playerInfo.worldLoaded = false;
                    }
                }
            }
        }

        blocks.parallelStream().forEach(this::updateBlock);

        if(!objectData.playerInfo.worldLoaded) return;

        SimpleCollisionBox waterBox = objectData.box.copy().expand(0, -.38, 0);

        waterBox.xMin = Math.floor(waterBox.xMin);
        waterBox.yMin = Math.floor(waterBox.yMin);
        waterBox.zMin = Math.floor(waterBox.zMin);
        waterBox.xMax = Math.floor(waterBox.xMax + 1.);
        waterBox.yMax = Math.floor(waterBox.yMax + 1.);
        waterBox.zMax = Math.floor(waterBox.zMax + 1.);

        SimpleCollisionBox lavaBox = objectData.box.copy().expand(-.1f, -.4f, -.1f);

        waterBox.xMin = Math.floor(waterBox.xMin);
        waterBox.yMin = Math.floor(waterBox.yMin);
        waterBox.zMin = Math.floor(waterBox.zMin);
        waterBox.xMax = Math.floor(waterBox.xMax + 1.);
        waterBox.yMax = Math.floor(waterBox.yMax + 1.);
        waterBox.zMax = Math.floor(waterBox.zMax + 1.);

        CollisionHandler handler = new CollisionHandler(blocks,
                Kauri.INSTANCE.entityProcessor.vehicles.getOrDefault(objectData.getPlayer().getUniqueId(), new ArrayList<>()),
                objectData.playerInfo.to);

        //Bukkit.broadcastMessage("chigga4");

        handler.setSize(0.6f, 0.05f);

        handler.setOffset(-0.1f);
        objectData.playerInfo.serverGround =
                handler.isCollidedWith(Materials.SOLID) || handler.contains(EntityType.BOAT);
        //Bukkit.broadcastMessage("chigga5");
        handler.setOffset(-0.4f);
        onSlab = handler.isCollidedWith(Materials.SLABS);
        onStairs = handler.isCollidedWith(Materials.STAIRS);
        onHalfBlock = onSlab || onStairs;

        handler.setOffset(-0.6f);
        handler.setSize(0.8f, 1f);
        miscNear = handler.isCollidedWith(XMaterial.CAKE.parseMaterial(),
                Material.valueOf(ProtocolVersion.getGameVersion().isBelow(ProtocolVersion.V1_13)
                        ? "CAKE_BLOCK" : "LEGACY_CAKE_BLOCK"),
                XMaterial.BREWING_STAND.parseMaterial(), XMaterial.FLOWER_POT.parseMaterial(),
                XMaterial.SKULL_ITEM.parseMaterial(), XMaterial.SNOW.parseMaterial(),
                XMaterial.WITHER_SKELETON_SKULL.parseMaterial(), XMaterial.SKELETON_WALL_SKULL.parseMaterial(),
                XMaterial.WITHER_SKELETON_WALL_SKULL.parseMaterial());

        if(!onHalfBlock) onHalfBlock = miscNear;

        handler.setSize(0.6f, 1.8f);
        handler.setSingle(true);
        onIce = handler.isCollidedWith(Materials.ICE);
        handler.setOffset(-0.02f);
        handler.setSingle(false);
        handler.setSize(0.602f, 1.802f);
        handler.setOffset(-0.001f);
        onSoulSand = handler.isCollidedWith(XMaterial.SOUL_SAND.parseMaterial());
        inWeb = handler.isCollidedWith(XMaterial.COBWEB.parseMaterial());
        onSlime = handler.isCollidedWith(XMaterial.SLIME_BLOCK.parseMaterial());

        inLava = handler.isCollidedWith(lavaBox, XMaterial.LAVA.parseMaterial(),
                XMaterial.STATIONARY_LAVA.parseMaterial());
        inWater = handler.isCollidedWith(waterBox, XMaterial.WATER.parseMaterial(), XMaterial.STATIONARY_WATER.parseMaterial());
        inLiquid = inLava || inWater;

        handler.setOffset(0);
        handler.setSize(0.599f, 1.8f);

        inBlock = handler.isCollidedWith(Materials.SOLID);

        if(objectData.playerInfo.deltaY <= 0) {
            onClimbable = objectData.playerInfo.blockOnTo != null
                    && BlockUtils.isClimbableBlock(objectData.playerInfo.blockOnTo);
        } else {
            handler.setSize(0.64f, 1.8f);
            onClimbable = handler.isCollidedWith(Materials.LADDER);
        }

        handler.setSize(0.6f, 2.4f);
        handler.setOffset(1.25f);
        blocksAbove = handler.isCollidedWith(Materials.SOLID);

        handler.setSize(2f, 1.79f);
        handler.setOffset(0.01f);
        blocksNear = handler.isCollidedWith(Materials.SOLID);

        if(objectData.boxDebuggers.size() > 0) {
            handler.setSize(0.62f, 1.81f);
            handler.setOffset(-0.01f);

            handler.getBlocks().stream().filter(block -> Materials.checkFlag(block.getType(), Materials.LIQUID))
                    .forEach(block -> {
                        objectData.boxDebuggers.forEach(pl -> {
                            List<SimpleCollisionBox> boxes = new ArrayList<>();
                             BlockData.getData(block.getType()).getBox(block, ProtocolVersion.getGameVersion()).downCast(boxes);

                             boxes.forEach(sbox -> {
                                 val max = sbox.max().subtract(block.getLocation().toVector());
                                 val min = sbox.min().subtract(block.getLocation().toVector());

                                 Vector subbed = max.subtract(min);

                                 pl.sendMessage("x=" + subbed.getX() + " y=" + subbed.getY() + " z=" + subbed.getZ());
                             });

                        });
                    });
            handler.getCollisionBoxes().forEach(cb -> cb.draw(WrappedEnumParticle.FLAME, objectData.boxDebuggers));
        }
        handler.setSize(0.6f, 1.8f);

        handler.setOffset(0f);

        SimpleCollisionBox box = getBox().expand(
                Math.abs(objectData.playerInfo.from.x - objectData.playerInfo.to.x) + 0.1f,
                -0.01f,
                Math.abs(objectData.playerInfo.from.z - objectData.playerInfo.to.z) + 0.1f);
        collidesHorizontally = !(horizontalCollisions = blockCollisions(handler.getBlocks(), box)).isEmpty();

        handler.setSize(0.8f, 2.8f);
        handler.setOffset(1f);

        aboveCollisions.clear();
        belowCollisions.clear();

        handler.getCollisionBoxes().forEach(cb -> cb.downCast(aboveCollisions));

        handler.setSize(0.8f, 0.8f);
        handler.setOffset(-1f);
        handler.getCollisionBoxes().forEach(cb -> cb.downCast(belowCollisions));

        handler.setSize(0.6f, 1.8f);
        handler.setOffset(0f);

        box = getBox().expand(0, 0.1f, 0);
        collidesVertically = !(verticalCollisions = blockCollisions(handler.getBlocks(), box)).isEmpty();

        this.handler = handler;
    }

    private final List<Location> lastUpdates = new CacheList<>(10, TimeUnit.SECONDS);
    private synchronized void updateBlock(Block block) {
        if(!lastUpdates.contains(block.getLocation())) {
            objectData.getPlayer().sendBlockChange(block.getLocation(), block.getType(), block.getData());
            lastUpdates.add(block.getLocation());
        }
    }

    public SimpleCollisionBox getBox() {
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
