package dev.brighten.anticheat.utils;

import cc.funkemunky.api.utils.BlockUtils;
import cc.funkemunky.api.utils.BoundingBox;
import dev.brighten.anticheat.data.ObjectData;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.*;

public class CollisionHandler {
    private ObjectData data;
    public boolean onGround, blocksNear, nearGround, collidesHorizontally, collidesVertically,
            onSlab, onStairs, onHalfBlock, inLiquid, inWeb, onSlime, onIce, onSoulSand, inWater, inLava, blocksAbove;
    public List<Block> blocksUnderPlayer = Collections.synchronizedList(new ArrayList<>());
    public List<Map.Entry<Block, BoundingBox>> boxesColliding = Collections.synchronizedList(new ArrayList<>()), allBlocks = Collections.synchronizedList(new ArrayList<>());

    public CollisionHandler(ObjectData data) {
        this.data = data;
    }

    public void onCollide(Block block, BoundingBox box, boolean entity) {
        if(data.box.collides(box) && block != null) {
            boxesColliding.add(new AbstractMap.SimpleEntry<>(block, box));
        }

        if(entity || BlockUtils.isSolid(block)) {
            if(box.collidesVertically(data.box
                    .subtract(0, 0.1f, 0, 0, 1, 0))) {
                onGround = nearGround = true;

                if(!entity) {
                    blocksUnderPlayer.add(block);
                    if(BlockUtils.isSlab(block)) {
                        onHalfBlock = onSlab = true;
                    } else if(BlockUtils.isStair(block)) {
                        onHalfBlock = onStairs = true;
                    } else if(block.getType().toString().contains("SLIME")) {
                        onSlime = true;
                    } else if(BlockUtils.isIce(block)) {
                        onIce = true;
                    } else if(block.getType().toString().contains("SOUL")) {
                        onSoulSand = true;
                    } else if(BlockUtils.isBed(block)
                            || block.getType().equals(Material.CAKE_BLOCK)
                            || block.getType().equals(Material.CAULDRON)
                            || BlockUtils.isTrapDoor(block)) {
                        onHalfBlock = true;
                    }
                }
            } else if(box.collidesVertically(data.box
                    .subtract(0, 1.5f, 0,0,0.25f,0))) {
                nearGround = true;
            }

            if(data.box.grow(0.3f, -0.1f, 0.3f).collidesHorizontally(box)) {
                blocksNear = true;
            }

            if(data.box.add(0,0.5f,0,0,0.5f,0).collidesVertically(box)) {
                blocksAbove = true;
            }

            if(data.box.collidesHorizontally(box)) {
                collidesHorizontally = true;
            }
            if(data.box.collidesVertically(box)) {
                collidesVertically = true;
            }
        } else {
            if(data.box.grow(0.05f,0.05f,0.05f).collides(box)) {
               if(BlockUtils.isLiquid(block)) {
                   inLiquid = true;

                   if(block.getType().toString().contains("LAVA")) {
                       inLava = true;
                   } else {
                       inLiquid = true;
                   }
               } else if(block.getType().toString().contains("WEB")) {
                   inWeb = true;
               }
            }
        }

        /*if(Atlas.getInstance().getCurrentTicks() % 5 == 0) {
            Kauri.INSTANCE.executor.execute(() -> MiscUtils.createParticlesForBoundingBox(data.getPlayer(), box, WrappedEnumParticle.FLAME, 0.25f));
        }*/
        if(!entity) {
            allBlocks.add(new AbstractMap.SimpleEntry<>(block, box));
        }
    }
}
