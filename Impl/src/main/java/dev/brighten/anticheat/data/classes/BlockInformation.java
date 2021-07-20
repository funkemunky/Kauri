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
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.Collectors;

public class BlockInformation {
    private ObjectData objectData;
    public boolean onClimbable, onSlab, onStairs, onHalfBlock, inLiquid, inLava, inWater, inWeb, onSlime, onIce,
            onSoulSand, blocksAbove, collidesVertically, bedNear, collidesHorizontally, blocksNear, inBlock, miscNear,
            collidedWithEntity, roseBush, inPortal, blocksBelow, pistonNear;
    public float currentFriction, fromFriction;
    public CollisionHandler
            handler = new CollisionHandler(new ArrayList<>(), new ArrayList<>(), new KLocation(0,0,0), null);
    public final List<SimpleCollisionBox> aboveCollisions = Collections.synchronizedList(new ArrayList<>()),
            belowCollisions = Collections.synchronizedList(new ArrayList<>());
    public final List<Block> blocks = Collections.synchronizedList(new ArrayList<>());
    private static final List<Material> skulls = new ArrayList<>();

    static {
        for (Material value : Material.values()) {
            if(value.name().contains("SKULL"))
                skulls.add(value);
        }
    }

    //Caching material
    private final Material cobweb = XMaterial.COBWEB.parseMaterial(), rosebush = XMaterial.ROSE_BUSH.parseMaterial();

    public BlockInformation(ObjectData objectData) {
        this.objectData = objectData;
    }

    public void runCollisionCheck() {
        if(!Kauri.INSTANCE.enabled
                || Kauri.INSTANCE.lastEnabled.isNotPassed(6))
            return;

        double dy = Math.abs(objectData.playerInfo.deltaY);
        double dh = objectData.playerInfo.deltaXZ;

        if(dh == 0 && dy == 0) return;

        blocks.clear();

        onClimbable = onSlab = onStairs = onHalfBlock = inLiquid = inLava = inWater = inWeb = onSlime = pistonNear
                = onIce = onSoulSand = blocksAbove = collidesVertically = bedNear = collidesHorizontally =
                blocksNear = inBlock = miscNear = collidedWithEntity = blocksBelow = inPortal = false;

        if(dy > 2) dy = 2;
        if(dh > 2) dh = 2;

        int startX = Location.locToBlock(objectData.playerInfo.to.x - 0.3 - dh);
        int endX = Location.locToBlock(objectData.playerInfo.to.x + 0.3 + dh);
        int startY = Location.locToBlock(objectData.playerInfo.to.y - 0.51 - dy);
        int endY = Location.locToBlock(objectData.playerInfo.to.y + 1.99 + dy);
        int startZ = Location.locToBlock(objectData.playerInfo.to.z - 0.3 - dh);
        int endZ = Location.locToBlock(objectData.playerInfo.to.z + 0.3 + dh);

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
        synchronized (belowCollisions) {
            belowCollisions.clear();
        }
        synchronized (aboveCollisions) {
            aboveCollisions.clear();
        }
        final World world = objectData.getPlayer().getWorld();
        int it = 9 * 9;
        start:
        for (int chunkx = startX >> 4; chunkx <= endX >> 4; ++chunkx) {
            int cx = chunkx << 4;

            for (int chunkz = startZ >> 4; chunkz <= endZ >> 4; ++chunkz) {
                if (!world.isChunkLoaded(chunkx, chunkz)) {
                    objectData.playerInfo.worldLoaded = false;
                    continue;
                }
                Chunk chunk = world.getChunkAt(chunkx, chunkz);
                if (chunk != null) {
                    int cz = chunkz << 4;
                    int xstart = startX < cx ? cx : startX;
                    int xend = endX < cx + 16 ? endX : cx + 16;
                    int zstart = startZ < cz ? cz : startZ;
                    int zend = endZ < cz + 16 ? endZ : cz + 16;

                    for (int x = xstart; x <= xend; ++x) {
                        for (int z = zstart; z <= zend; ++z) {
                            for (int y = startY < 0 ? 0 : startY; y <= endY; ++y) {
                                if (it-- <= 0) {
                                    break start;
                                }
                                Block block = chunk.getBlock(x & 15, y, z & 15);
                                if (block.getType() != Material.AIR) {
                                    blocks.add(block);

                                    final Material type = block.getType();
                                    CollisionBox blockBox = BlockData.getData(type)
                                            .getBox(block, objectData.playerVersion);

                                    if(type.equals(cobweb) && blockBox.isCollided(normalBox))
                                        inWeb = true;

                                    if(type.equals(rosebush))
                                        roseBush = true;

                                    if(normalBox.copy().offset(0, 0.6f, 0).isCollided(blockBox))
                                        blocksAbove = true;

                                    if(normalBox.copy().expand(1, -0.0001, 1).isIntersected(blockBox))
                                        blocksNear = true;

                                    if(normalBox.copy().expand(0.1, 0, 0.1)
                                            .offset(0, 1,0).isCollided(blockBox)) {
                                        synchronized (aboveCollisions) {
                                            blockBox.downCast(aboveCollisions);
                                        }
                                    }

                                    if(normalBox.copy().expand(0.1, 0, 0.1).offset(0, -1, 0)
                                            .isCollided(blockBox)) {
                                        synchronized (belowCollisions) {
                                            blockBox.downCast(belowCollisions);
                                        }
                                    }

                                    if(Materials.checkFlag(type, Materials.WATER)) {
                                        if(waterBox.isCollided(blockBox))
                                            inWater = inLiquid = true;
                                    } else if(Materials.checkFlag(type, Materials.LAVA)) {
                                        if(lavaBox.isCollided(blockBox))
                                            inLava = inLiquid = true;
                                    } else if(Materials.checkFlag(type, Materials.SOLID)) {
                                        SimpleCollisionBox groundBox = normalBox.copy()
                                                .offset(0, -.1, 0).expandMax(0, -1.2, 0);
                                        byte data = block.getData();
                                        XMaterial blockMaterial =
                                                XMaterial.requestXMaterial(type.name(), data);

                                        if(normalBox.copy().expand(0.1, 0, 0.1).expandMin(0, -1, 0)
                                                .isIntersected(blockBox))
                                            blocksBelow = true;

                                        if(normalBox.isIntersected(blockBox)) inBlock = true;

                                        SimpleCollisionBox box = objectData.box.copy();

                                        box.expand(Math.abs(objectData.playerInfo.deltaX) + 0.1, -0.001,
                                                Math.abs(objectData.playerInfo.deltaZ) + 0.1);
                                        if (Helper.isCollided(handler.getBlocks(), box))
                                            collidesHorizontally = true;

                                        box = objectData.box.copy();
                                        box.expand(0, 0.1, 0);

                                        if (Helper.isCollided(handler.getBlocks(), box))
                                            collidesVertically = true;

                                        if(groundBox.isIntersected(blockBox)) {
                                            objectData.playerInfo.serverGround = true;

                                            if(blockMaterial != null)
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
                                                && Materials.checkFlag(type, Materials.LADDER)
                                                && normalBox.copy().expand(0.02f, 0, 0.02f).isCollided(blockBox)) {
                                            onClimbable = true;
                                        }

                                        if(blockMaterial != null && normalBox.copy().expand(0.5, 0.5, 0.5)
                                                .isCollided(blockBox)) {
                                            switch (blockMaterial) {
                                                case PISTON:
                                                case PISTON_HEAD:
                                                case MOVING_PISTON:
                                                case STICKY_PISTON: {
                                                    pistonNear = true;
                                                    break;
                                                }
                                            }
                                        }

                                        if(groundBox.copy().expand(0.5, 0.3, 0.5).isCollided(blockBox)) {
                                            if(Materials.checkFlag(type, Materials.SLABS))
                                                onSlab = true;
                                            if(Materials.checkFlag(type, Materials.STAIRS))
                                                onStairs = true;

                                            if(blockMaterial != null)
                                            switch(blockMaterial) {
                                                case CAKE:
                                                case BREWING_STAND:
                                                case FLOWER_POT:
                                                case SKULL:
                                                case PLAYER_HEAD:
                                                case PLAYER_WALL_HEAD:
                                                case SKELETON_SKULL:
                                                case CREEPER_HEAD:
                                                case DRAGON_HEAD:
                                                case ZOMBIE_HEAD:
                                                case ZOMBIE_WALL_HEAD:
                                                case CREEPER_WALL_HEAD:
                                                case DRAGON_WALL_HEAD:
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
                                    } else if(blockBox.isCollided(normalBox)) {
                                        XMaterial blockMaterial =
                                                XMaterial.requestXMaterial(type.name(), block.getData());

                                        if(blockMaterial != null)
                                            switch(blockMaterial) {
                                                case END_PORTAL:
                                                case NETHER_PORTAL: {
                                                    inPortal = true;
                                                    break;
                                                }
                                            }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if(!objectData.playerInfo.worldLoaded)
            return;

        CollisionHandler handler = new CollisionHandler(blocks,
                objectData.playerInfo.nearbyEntities,
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

            for (Block block : handler.getBlocks()) {
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
            }
            handler.setSize(0.6, 1.8);
            handler.getCollisionBoxes().forEach(cb -> cb.draw(WrappedEnumParticle.FLAME, objectData.boxDebuggers));
        }

        this.handler.getEntities().clear();
        this.handler = null;
        this.handler = handler;
    }

    public SimpleCollisionBox getBox() {
        return new SimpleCollisionBox(objectData.playerInfo.to.toVector(), objectData.playerInfo.to.toVector())
                .expand(0.3, 0,0.3).expandMax(0, 1.8, 0);
    }
}
