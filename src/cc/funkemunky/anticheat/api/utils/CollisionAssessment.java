package cc.funkemunky.anticheat.api.utils;

import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.utils.BlockUtils;
import cc.funkemunky.api.utils.BoundingBox;
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
    private boolean onGround, fullyInAir, inLiquid, blocksOnTop, pistonsNear, onHalfBlock, onClimbable, onIce, collidesHorizontally, inWeb, onSlime;
    private Set<Material> materialsCollided;
    private BoundingBox playerBox;

    public CollisionAssessment(BoundingBox playerBox, PlayerData data) {
        onGround = inLiquid = blocksOnTop = pistonsNear = onHalfBlock = onClimbable = onIce = collidesHorizontally = inWeb = onSlime = false;
        fullyInAir = true;
        this.data = data;
        this.playerBox = playerBox;
        materialsCollided = new HashSet<>();
    }

    public void assessBox(BoundingBox bb, World world, boolean isEntity) {
        Location location = bb.getMinimum().toLocation(world);
        Block block = location.getBlock();

        if (BlockUtils.isSolid(block) || isEntity) {
            if (bb.getMinimum().getY() < (playerBox.getMinimum().getY() + 0.1) && bb.collidesVertically(playerBox.subtract(0, 0.001f, 0, 0, 0, 0))) {
                onGround = true;
            }

            if ((bb.getMaximum().getY() + 0.1) > playerBox.getMaximum().getY() && bb.collidesVertically(playerBox.add(0, 0, 0, 0, 0.35f, 0))) {
                blocksOnTop = true;
            }

            if (BlockUtils.isPiston(block)) {
                pistonsNear = true;
            }

            if (BlockUtils.isSlab(block) || BlockUtils.isStair(block) || block.getType().getId() == 92 || block.getType().getId() == 397) {
                onHalfBlock = true;
            }

            if (BlockUtils.isIce(block) && playerBox.subtract(0, 0.5f, 0, 0, 0, 0).collidesVertically(bb)) {
                onIce = true;
            }

            if (bb.collidesHorizontally(playerBox.grow(0.05f + (ProtocolVersion.getGameVersion().isOrAbove(ProtocolVersion.V1_13) ? 0.05f : 0), 0, 0.05f + (ProtocolVersion.getGameVersion().isOrAbove(ProtocolVersion.V1_13) ? 0.05f : 0)))) {
                collidesHorizontally = true;

            }

            if (BlockUtils.isClimbableBlock(block) && playerBox.grow(0.3f, 0, 0.3f).collidesHorizontally(bb)) {
                onClimbable = true;
            }
        } else {
            if (BlockUtils.isLiquid(block) && playerBox.collidesVertically(bb)) {
                inLiquid = true;
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