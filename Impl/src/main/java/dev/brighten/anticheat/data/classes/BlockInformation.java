package dev.brighten.anticheat.data.classes;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.types.enums.WrappedEnumParticle;
import cc.funkemunky.api.utils.BlockUtils;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.Materials;
import cc.funkemunky.api.utils.XMaterial;
import cc.funkemunky.api.utils.world.BlockData;
import cc.funkemunky.api.utils.world.CollisionBox;
import cc.funkemunky.api.utils.world.EntityData;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.utils.CollisionHandler;
import dev.brighten.anticheat.utils.Helper;
import lombok.val;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.Collectors;

public class BlockInformation {
    private ObjectData objectData;
    public boolean onClimbable, onSlab, onStairs, onHalfBlock, inLiquid, inLava, inWater, inWeb, onSlime, onIce,
            onSoulSand, blocksAbove, collidesVertically, bedNear, collidesHorizontally, blocksNear, inBlock, miscNear,
            collidedWithEntity, roseBush;
    public float currentFriction, fromFriction;
    public CollisionHandler
            handler = new CollisionHandler(new ArrayList<>(), new ArrayList<>(), new KLocation(0,0,0), null);
    public List<SimpleCollisionBox> aboveCollisions = new ArrayList<>(), belowCollisions = new ArrayList<>();
    public final List<Block> blocks = Collections.synchronizedList(new ArrayList<>());

    public BlockInformation(ObjectData objectData) {
        this.objectData = objectData;
    }

    private static Material[] bedBlocks =
            Arrays.stream(Material.values()).filter(mat -> mat.name().contains("BED")).toArray(Material[]::new);

    public void runCollisionCheck() {
        if(!Kauri.INSTANCE.enabled
                || Kauri.INSTANCE.lastEnabled.isNotPassed(6))
            return;
        blocks.clear();

        onClimbable = onSlab = onStairs = onHalfBlock = inLiquid = inLava = inWater = inWeb = onSlime
                = onIce = onSoulSand = blocksAbove = collidesVertically = bedNear = collidesHorizontally =
                blocksNear = inBlock = miscNear = collidedWithEntity = false;

        double dy = Math.abs(objectData.playerInfo.deltaY);
        double dh = objectData.playerInfo.deltaXZ;

        if(dy > 2) dy = 2;
        if(dh > 2) dh = 2;

        int startX = Location.locToBlock(objectData.playerInfo.to.x - 1 - dh);
        int endX = Location.locToBlock(objectData.playerInfo.to.x + 1 + dh);
        int startY = Location.locToBlock(objectData.playerInfo.to.y - 1 - dy);
        int endY = Location.locToBlock(objectData.playerInfo.to.y + 3 + dy);
        int startZ = Location.locToBlock(objectData.playerInfo.to.z - 1 - dh);
        int endZ = Location.locToBlock(objectData.playerInfo.to.z + 1 + dh);

        SimpleCollisionBox waterBox = objectData.box.copy().expand(0, -.38, 0);

        waterBox.xMin = Math.floor(waterBox.xMin);
        waterBox.yMin = Math.floor(waterBox.yMin);
        waterBox.zMin = Math.floor(waterBox.zMin);
        waterBox.xMax = Math.floor(waterBox.xMax + 1.);
        waterBox.yMax = Math.floor(waterBox.yMax + 1.);
        waterBox.zMax = Math.floor(waterBox.zMax + 1.);

        SimpleCollisionBox lavaBox = objectData.box.copy().expand(-.1f, -.4f, -.1f);

        lavaBox.xMin = Math.floor(waterBox.xMin);
        lavaBox.yMin = Math.floor(waterBox.yMin);
        lavaBox.zMin = Math.floor(waterBox.zMin);
        lavaBox.xMax = Math.floor(waterBox.xMax + 1.);
        lavaBox.yMax = Math.floor(waterBox.yMax + 1.);
        lavaBox.zMax = Math.floor(waterBox.zMax + 1.);

        SimpleCollisionBox normalBox = objectData.box.copy();

        objectData.playerInfo.worldLoaded = true;
        objectData.playerInfo.lServerGround = objectData.playerInfo.serverGround;
        belowCollisions.clear();
        aboveCollisions.clear();
        for(int x = startX ; x < endX ; x++) {
            for(int y = startY ; y < endY ; y++) {
                for(int z = startZ ; z < endZ ; z++) {
                    Location loc = new Location(objectData.getPlayer().getWorld(), x, y, z);
                    Block block = BlockUtils.getBlock(loc);

                    if(block != null) {
                        blocks.add(block);
                        CollisionBox blockBox = BlockData.getData(block.getType())
                                .getBox(block, objectData.playerVersion);

                        if(block.getType().equals(XMaterial.COBWEB.parseMaterial()) && blockBox.isCollided(normalBox))
                            inWeb = true;

                        if(block.getType().equals(XMaterial.ROSE_BUSH.parseMaterial()))
                            roseBush = true;

                        if(normalBox.copy().offset(0, 0.6f, 0).isCollided(blockBox))
                            blocksAbove = true;

                        if(normalBox.copy().expand(1, -0.0001, 1).isIntersected(blockBox))
                            blocksNear = true;

                        if(normalBox.copy().expand(0.1, 0, 0.1)
                                .offset(0, 1,0).isCollided(blockBox))
                            blockBox.downCast(aboveCollisions);

                        if(normalBox.copy().expand(0.1, 0, 0.1).offset(0, -1, 0)
                                .isCollided(blockBox))
                            blockBox.downCast(belowCollisions);

                        if(Materials.checkFlag(block.getType(), Materials.WATER)) {
                            if(waterBox.isCollided(blockBox))
                                inWater = inLiquid = true;
                        } else if(Materials.checkFlag(block.getType(), Materials.LAVA)) {
                            if(lavaBox.isCollided(blockBox))
                                inLava = inLiquid = true;
                        } else if(Materials.checkFlag(block.getType(), Materials.SOLID)) {
                            SimpleCollisionBox groundBox = normalBox.copy()
                                    .offset(0, -.1, 0).expandMax(0, -1.2, 0);
                            XMaterial blockMaterial =
                                    XMaterial.requestXMaterial(block.getType().name(), block.getData());

                            if(blockMaterial == null) continue;

                            if(normalBox.isIntersected(blockBox)) inBlock = true;

                            if(Helper.isCollidedVertically(normalBox, blockBox))
                                collidesVertically = true;

                            if(Helper.isCollidedHorizontally(normalBox, blockBox))
                                collidesHorizontally = true;

                            if(groundBox.isIntersected(blockBox)) {
                                objectData.playerInfo.serverGround = true;

                                switch (blockMaterial) {
                                    case ICE:
                                    case BLUE_ICE:
                                    case FROSTED_ICE:
                                    case PACKED_ICE: {
                                        onIce = true;
                                        break;
                                    }
                                    case SOUL_SAND: {
                                        onSoulSand = true;
                                        break;
                                    }
                                    case SLIME_BLOCK: {
                                        onSlime = true;
                                        break;
                                    }
                                }
                            }
                            if(objectData.playerInfo.deltaY > 0
                                    && Materials.checkFlag(block.getType(), Materials.LADDER)
                                    && normalBox.copy().expand(0.02f, 0, 0.02f).isCollided(blockBox)) {
                                onClimbable = true;
                            }

                            if(groundBox.expand(0.1, 0, 0.1).isIntersected(blockBox)) {
                                if(Materials.checkFlag(block.getType(), Materials.SLABS))
                                    onSlab = true;
                                if(Materials.checkFlag(block.getType(), Materials.STAIRS))
                                    onStairs = true;

                                switch(blockMaterial) {
                                    case CAKE:
                                    case BREWING_STAND:
                                    case FLOWER_POT:
                                    case SKULL:
                                    case SKELETON_SKULL:
                                    case WITHER_SKELETON_SKULL:
                                    case SKELETON_WALL_SKULL:
                                    case WITHER_SKELETON_WALL_SKULL:
                                    case SNOW: {
                                        miscNear = true;
                                        break;
                                    }
                                    case BLACK_BED:
                                    case BLUE_BED:
                                    case BROWN_BED:
                                    case CYAN_BED:
                                    case GRAY_BED:
                                    case GREEN_BED:
                                    case LIME_BED:
                                    case MAGENTA_BED:
                                    case ORANGE_BED:
                                    case PINK_BED:
                                    case PURPLE_BED:
                                    case RED_BED:
                                    case WHITE_BED:
                                    case YELLOW_BED:
                                    case LIGHT_BLUE_BED:
                                    case LIGHT_GRAY_BED: {
                                        bedNear = true;
                                        break;
                                    }
                                }
                            }
                        }
                    } else {
                        objectData.playerInfo.worldLoaded = false;
                        break;
                    }
                }
            }
        }

        if(!objectData.playerInfo.worldLoaded)
            return;

        CollisionHandler handler = new CollisionHandler(blocks,
                Kauri.INSTANCE.entityProcessor.allEntitiesNearPlayer.getOrDefault(objectData.uuid, new ArrayList<>()),
                objectData.playerInfo.to, objectData);

        //Bukkit.broadcastMessage("chigga4");

        for (Entity entity : handler.getEntities()) {
            CollisionBox entityBox = EntityData.getEntityBox(entity.getLocation(), entity);

            if(entityBox == null) continue;

            if(entityBox.isCollided(normalBox.copy().offset(0, -.1, 0)))
                objectData.playerInfo.serverGround = true;

            if(entityBox.isCollided(normalBox))
                collidedWithEntity = true;
        }

        //Bukkit.broadcastMessage("chigga5");
        onHalfBlock = onSlab || onStairs || miscNear || bedNear;

        if(objectData.playerInfo.deltaY <= 0) {
            onClimbable = objectData.playerInfo.blockOnTo != null
                    && BlockUtils.isClimbableBlock(objectData.playerInfo.blockOnTo);
        }

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

        this.handler = handler;
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
