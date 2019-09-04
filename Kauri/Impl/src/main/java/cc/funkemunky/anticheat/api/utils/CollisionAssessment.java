package cc.funkemunky.anticheat.api.utils;

import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.api.utils.BlockUtils;
import cc.funkemunky.api.utils.BoundingBox;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.*;

@Getter
@Setter
/* We use this to process the bounding boxes collided around the player for our checks to use as utils */
public class CollisionAssessment {
    private PlayerData data;
    private boolean onGround, halfBlocksAround, nearGround, liquidAllAround, fullyInAir, inLiquid, liquidBelow, nearLiquid, blocksOnTop, pistonsNear, onHalfBlock, onClimbable, onIce, collidesHorizontally, inWeb, onSlime, onSoulSand, blocksNear, blocksAround;
    private Set<Material> materialsCollided;
    private List<Float> blockHeights;
    private BoundingBox playerBox;
    private int liquidTicks;
    public CollisionAssessment(BoundingBox playerBox, PlayerData data) {
        fullyInAir = true;
        this.data = data;
        this.playerBox = playerBox;
        materialsCollided = new HashSet<>();
        blockHeights = Collections.synchronizedList(new ArrayList<>());
    }

    public void assessBox(BoundingBox bb, World world, boolean isEntity) {
        Location location = bb.getMinimum().toLocation(world);
        Block block = BlockUtils.getBlock(location);

        if(block == null && !isEntity) return;

        if (isEntity || (!block.getType().equals(Material.AIR) && BlockUtils.isSolid(block))) {
            if(isEntity) bb = bb.grow(0.35f, 0.2f, 0.35f);
            if (bb.collidesVertically(playerBox.subtract(0, 0.12f, 0, 0, 1f, 0))) {
                onGround = true;
                nearGround = true;

                if(!isEntity && block.getType().equals(Material.SOUL_SAND)) {
                    onSoulSand = true;
                }
            } else if(bb.collidesVertically(playerBox.subtract(0, 0.8f,0,0,1f,0))) {
                nearGround = true;
            }

            if(bb.collides(playerBox.subtract(0, 0.6f,0,0,0.2f,0).grow(1f, 0, 1f))) {
                blockHeights.add(bb.maxY - bb.minY);
            }

            if (bb.collidesHorizontally(playerBox.grow(0.05f, 0, 0.05f))) {
                collidesHorizontally = true;
            }

            if (!isEntity) {
                if (bb.collidesVertically(playerBox.add(0, 1.45f, 0, 0, 0.35f, 0))) {
                    blocksOnTop = true;
                }

                if (BlockUtils.isPiston(block)) {
                    pistonsNear = true;
                } else if(BlockUtils.isSlab(block) || BlockUtils.isSlab(block) || block.getType().toString().contains("CAKE") || block.getType().toString().contains("SNOW") || block.getType().toString().contains("DAYLIGHT_DETECTOR") || block.getType().toString().contains("SKULL") || block.getType().toString().contains("BED") || block.getType().toString().contains("DIODE") || BlockUtils.isStair(block) || block.getType().getId() == 92 || block.getType().getId() == 397) {
                    if(bb.intersectsWithBox(getData().getBoundingBox().grow(0.2f,1f,0.2f))) {
                        onHalfBlock = true;
                    }
                    halfBlocksAround = true;
                } else if(BlockUtils.isFence(block))  {
                    if(getData().getBoundingBox().subtract(0,0.25f,0,0,0,0).collides(bb)) {
                        onHalfBlock = true;
                    }
                } else if(BlockUtils.isClimbableBlock(block)) {
                    if(bb.intersectsWithBox(getData().getBoundingBox().grow(0.6f, 0.1f, 0.6f))) {
                        onClimbable = true;
                    }
                } else if (playerBox.subtract(0, 1f, 0, 0, 0.5f, 0).collidesVertically(bb)) {
                    if(BlockUtils.isIce(block)) {
                        onIce = true;
                    } else if(block.getType().toString().contains("SLIME")) {
                        onSlime = true;
                    }
                }
                if (bb.intersectsWithBox(data.getBoundingBox().grow(1.0f, 0, 1.0f).shrink(0, 0.01f, 0))) {
                    blocksNear = blocksAround = true;
                } else if (bb.intersectsWithBox(getData().getBoundingBox().grow(1.5f, 1.5f, 1.5f))) {
                    blocksAround = true;
                }
            }
        } else {
            if (BlockUtils.isLiquid(block)) {
                if(playerBox.collides(bb.grow(0.1f,0,0.1f))) {
                    nearLiquid = inLiquid = true;
                } else if(playerBox.subtract(0,0.025f,0,0,0,0).collides(bb)) {
                    nearLiquid = liquidBelow = true;
                } else if(playerBox.grow(0.2f, 0.1f, 0.2f).collides(bb)) {
                    nearLiquid = true;
                }
                if(playerBox.grow(1f, 0, 1f).subtract(0, 0.5f, 0,0,1.2f,0f).collides(bb)) {
                    if(liquidTicks++ >= 6) {
                        liquidAllAround = true;
                    }
                }
            }
            if (block.getType().toString().contains("WEB") && playerBox.collidesVertically(bb)) {
                inWeb = true;
            }
        }

        addMaterial(location.getBlock());
    }


    private void addMaterial(Block block) {
        materialsCollided.add(block.getType());
    }
}