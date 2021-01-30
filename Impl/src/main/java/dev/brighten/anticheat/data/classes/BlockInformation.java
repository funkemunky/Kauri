package dev.brighten.anticheat.data.classes;

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
import dev.brighten.anticheat.utils.CacheList;
import dev.brighten.anticheat.utils.CollisionHandler;
import dev.brighten.anticheat.utils.Helper;
import lombok.val;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BlockInformation {
    private ObjectData objectData;
    public boolean onClimbable, onSlab, onStairs, onHalfBlock, inLiquid, inLava, inWater, inWeb, onSlime, onIce,
            onSoulSand, blocksAbove, collidesVertically, bedNear, collidesHorizontally, blocksNear, inBlock, miscNear,
            collidedWithEntity;
    public float currentFriction, fromFriction;
    public CollisionHandler
            handler = new CollisionHandler(new ArrayList<>(), new ArrayList<>(), new KLocation(0,0,0), null);
    public List<Block> verticalCollisions, horizontalCollisions;
    public List<SimpleCollisionBox> aboveCollisions = new ArrayList<>(), belowCollisions = new ArrayList<>();
    public final List<Block> blocks = Collections.synchronizedList(new ArrayList<>());

    public BlockInformation(ObjectData objectData) {
        this.objectData = objectData;
    }

    private static Material[] bedBlocks =
            Arrays.stream(Material.values()).filter(mat -> mat.name().contains("BED")).toArray(Material[]::new);

    public void runCollisionCheck() {
        if(!Kauri.INSTANCE.enabled
                || Kauri.INSTANCE.lastEnabled.isNotPassed(6)) return;
        synchronized (blocks) {
            blocks.clear();
        }

        double dy = Math.abs(objectData.playerInfo.deltaY);
        double dh = objectData.playerInfo.deltaXZ;

        if(dy > 2) dy = 2;
        if(dh > 2) dh = 2;

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

        if(Kauri.INSTANCE.keepaliveProcessor.currentKeepalive.start % 5 == 0)
            for (Block block : blocks) updateBlock(block);

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
                Kauri.INSTANCE.entityProcessor.allEntitiesNearPlayer.getOrDefault(objectData.uuid, new ArrayList<>()),
                objectData.playerInfo.to, objectData);

        //Bukkit.broadcastMessage("chigga4");

        handler.setSize(0.6, 0.05f);

        handler.setOffset(-0.1f);
        handler.intersectsWithFuture(Materials.SOLID,
                i -> objectData.playerInfo.serverGround = i || handler.contains(EntityType.BOAT));
        //Bukkit.broadcastMessage("chigga5");
        handler.setOffset(-0.4f);
        handler.intersectsWithFuture(Materials.SLABS, i -> {
            onSlab = i;
        });

        handler.intersectsWithFuture(Materials.STAIRS, i -> {
            onStairs = i;
        });

        handler.setOffset(-0.6f);
        handler.setSize(0.8f, 1f);
        miscNear = handler.isCollidedWith(XMaterial.CAKE.parseMaterial(),
                Material.valueOf(ProtocolVersion.getGameVersion().isBelow(ProtocolVersion.V1_13)
                        ? "CAKE_BLOCK" : "LEGACY_CAKE_BLOCK"),
                XMaterial.BREWING_STAND.parseMaterial(), XMaterial.FLOWER_POT.parseMaterial(),
                XMaterial.SKULL_ITEM.parseMaterial(), XMaterial.SNOW.parseMaterial(),
                XMaterial.WITHER_SKELETON_SKULL.parseMaterial(), XMaterial.SKELETON_WALL_SKULL.parseMaterial(),
                XMaterial.WITHER_SKELETON_WALL_SKULL.parseMaterial());

        bedNear = handler.isCollidedWith(bedBlocks);

        handler.setSize(0.6f, 1.8f);
        handler.collidesWithFuture(Materials.ICE, i -> onIce = i);
        handler.setOffset(-0.02f);
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

        handler.collidesWithFuture(Materials.SOLID, i -> inBlock = i);

        if(objectData.playerInfo.deltaY <= 0) {
            onClimbable = objectData.playerInfo.blockOnTo != null
                    && BlockUtils.isClimbableBlock(objectData.playerInfo.blockOnTo);
        } else {
            handler.setSize(0.64f, 1.8f);
            onClimbable = handler.isCollidedWith(Materials.LADDER);
        }

        handler.setSize(0.6f, 2.4f);
        handler.setOffset(1.25f);
        blocksAbove = handler.isCollidedWith();

        handler.setSize(2f, 1.79f);
        handler.setOffset(0.01f);
        blocksNear = handler.isCollidedWith();

        if(objectData.boxDebuggers.size() > 0) {
            handler.setSize(0.62f, 1.81f);
            handler.setOffset(-0.01f);

            handler.getBlocks().stream().filter(block -> Materials.checkFlag(block.getType(), Materials.LIQUID))
                    .forEach(block -> {
                        objectData.boxDebuggers.forEach(pl -> {
                            List<SimpleCollisionBox> boxes = new ArrayList<>();
                             BlockData.getData(block.getType()).getBox(block, ProtocolVersion.getGameVersion())
                                     .downCast(boxes);

                             boxes.forEach(sbox -> {
                                 val max = sbox.max().subtract(block.getLocation().toVector());
                                 val min = sbox.min().subtract(block.getLocation().toVector());

                                 Vector subbed = max.subtract(min);

                                 pl.sendMessage("x=" + subbed.getX() + " y=" + subbed.getY() + " z=" + subbed.getZ());
                             });

                        });
                    });
            handler.setSize(0.8f, 2f);
            handler.setOffset(-.2f);
            handler.getCollisionBoxes().forEach(cb -> cb.draw(WrappedEnumParticle.FLAME, objectData.boxDebuggers));
        }
        handler.setSize(0.6f, 1.8f);

        handler.setOffset(0f);

        SimpleCollisionBox box = getBox().expand(
                Math.abs(objectData.playerInfo.from.x - objectData.playerInfo.to.x) + 0.1f,
                -0.01f,
                Math.abs(objectData.playerInfo.from.z - objectData.playerInfo.to.z) + 0.1f);

        handler.setSize(0.62, 1.79);
        handler.setOffset(0.01);
        collidesHorizontally = !(horizontalCollisions = blockCollisions(handler.getBlocks(), box)).isEmpty()
                || handler.isCollidedWith();

        handler.setSize(0.8f, 2.8f);
        handler.setOffset(1f);

        aboveCollisions.clear();
        belowCollisions.clear();

        handler.getCollisionBoxes().forEach(cb -> cb.downCast(aboveCollisions));

        handler.setSize(0.8f, 0.8f);
        handler.setOffset(-1f);
        handler.getCollisionBoxes().forEach(cb -> cb.downCast(belowCollisions));

        box = getBox().expand(0, 0.1f, 0);

        handler.setSize(0.59, 1.81);
        handler.setOffset(-0.01);
        collidesVertically = !(verticalCollisions = blockCollisions(handler.getBlocks(), box)).isEmpty()
                || handler.isCollidedWith();

        handler.setSize(0.6f, 1.8f);
        handler.setOffset(0f);

        collidedWithEntity = handler.isCollidedWithEntity();

        handler.runFutures();
        onHalfBlock = onSlab || onStairs;
        if(!onHalfBlock) onHalfBlock = miscNear || bedNear;
        this.handler = handler;
    }

    private final List<Location> lastUpdates = new CacheList<>(10, TimeUnit.SECONDS);
    private synchronized void updateBlock(Block block) {
        if(!lastUpdates.contains(block.getLocation())
                && !objectData.playerInfo.shitMap.containsKey(block.getLocation())) {
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
