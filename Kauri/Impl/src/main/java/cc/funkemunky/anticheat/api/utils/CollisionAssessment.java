package cc.funkemunky.anticheat.api.utils;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.api.tinyprotocol.packet.types.WrappedEnumParticle;
import cc.funkemunky.api.utils.BlockUtils;
import cc.funkemunky.api.utils.BoundingBox;
import cc.funkemunky.api.utils.MiscUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
/* We use this to process the bounding boxes collided around the player for our checks to use as utils */
public class CollisionAssessment {
    private PlayerData data;
    private boolean onGround, nearGround, fullyInAir, inLiquid, liquidBelow, nearLiquid, blocksOnTop, pistonsNear, onHalfBlock, onClimbable, onIce, collidesHorizontally, inWeb, onSlime, onSoulSand, blocksNear, blocksAround;
    private Set<Material> materialsCollided;
    private BoundingBox playerBox;

    public CollisionAssessment(BoundingBox playerBox, PlayerData data) {
        fullyInAir = true;
        this.data = data;
        this.playerBox = playerBox;
        materialsCollided = new HashSet<>();
    }

    public void assessBox(BoundingBox bb, World world, boolean isEntity) {
        Location location = bb.getMinimum().toLocation(world);
        Block block = BlockUtils.getBlock(location);

        if(block == null && !isEntity) return;

        if (isEntity || (!block.getType().equals(Material.AIR) && BlockUtils.isSolid(block))) {
            if(isEntity) bb = bb.grow(0.35f, 0.2f, 0.35f);
            if (bb.collidesVertically(playerBox.subtract(0, 0.1f, 0, 0, 1.4f, 0))) {
                onGround = true;

                if(!isEntity && block.getType().equals(Material.SOUL_SAND)) {
                    onSoulSand = true;
                }
            }

            if (getData().isDebuggingBox() && bb.collides(playerBox) && Kauri.getInstance().getCurrentTicks() % 2 == 0) {
                BoundingBox box = bb;
                Kauri.getInstance().getExecutorService().submit(() -> MiscUtils.createParticlesForBoundingBox(getData().getPlayer(), box, WrappedEnumParticle.FLAME, 0.25f));
            }

            nearGround = true;

            if (bb.collidesHorizontally(playerBox.grow(0.05f, 0, 0.05f))) {
                collidesHorizontally = true;
            }

            if (!isEntity) {
                if (bb.collidesVertically(playerBox.add(0, 1.45f, 0, 0, 0.35f, 0))) {
                    blocksOnTop = true;
                }

                if (BlockUtils.isPiston(block)) {
                    pistonsNear = true;
                } else if(BlockUtils.isSlab(block) || BlockUtils.isSlab(block) || block.getType().toString().contains("SNOW") || block.getType().toString().contains("DAYLIGHT_DETECTOR") || block.getType().toString().contains("SKULL") || BlockUtils.isStair(block) || block.getType().getId() == 92 || block.getType().getId() == 397) {
                    if(bb.intersectsWithBox(data.getBoundingBox().grow(0.2f,1f,0.2f))) {
                        onHalfBlock = true;
                    }
                } else if(BlockUtils.isFence(block))  {
                    if(data.getBoundingBox().subtract(0,0.25f,0,0,0,0).collides(bb)) {
                        onHalfBlock = true;
                    }
                } else if(BlockUtils.isClimbableBlock(block)) {
                    if(bb.intersectsWithBox(data.getBoundingBox().grow(0.6f, 0.1f, 0.6f))) {
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
                } else if (bb.intersectsWithBox(data.getBoundingBox().grow(1.5f, 1.5f, 1.5f))) {
                    blocksAround = true;
                }
            }
        } else {
            if (BlockUtils.isLiquid(block)) {
                if(playerBox.intersectsWithBox(bb)) {
                    nearLiquid = inLiquid = true;
                } else if(playerBox.subtract(0,0.025f,0,0,0,0).collides(bb)) {
                    nearLiquid = liquidBelow = true;
                } else if(playerBox.grow(0.2f, 0.1f, 0.2f).collides(bb)) {
                    nearLiquid = true;
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