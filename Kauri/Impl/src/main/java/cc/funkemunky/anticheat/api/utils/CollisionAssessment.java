package cc.funkemunky.anticheat.api.utils;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
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
    private boolean onGround, fullyInAir, inLiquid, blocksOnTop, pistonsNear, onHalfBlock, onClimbable, onIce, collidesHorizontally, inWeb, onSlime, onSoulSand, blocksNear, blocksAround;
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
        Block block = BlockUtils.getBlock(location);

        if(block == null && !isEntity) return;

        if (isEntity || BlockUtils.isSolid(block)) {
            if (bb.collidesVertically(playerBox.subtract(0, 0.1f, 0, 0, 1.4f, 0))) {
                onGround = true;
            }

            if (getData().isDebuggingBox() && bb.collides(playerBox) && Kauri.getInstance().getCurrentTicks() % 2 == 0) {
                Atlas.getInstance().getThreadPool().submit(() -> MiscUtils.createParticlesForBoundingBox(getData().getPlayer(), bb, WrappedEnumParticle.FLAME, 0.25f));
            }

            if (bb.collidesVertically(playerBox.add(0, 1.45f, 0, 0, 0.35f, 0))) {
                blocksOnTop = true;
            }

            if (BlockUtils.isPiston(block)) {
                pistonsNear = true;
            }

            if ((BlockUtils.isSlab(block) || block.getType().toString().contains("DAYLIGHT_DETECTOR") || BlockUtils.isStair(block) || block.getType().getId() == 92 || block.getType().getId() == 397) && bb.intersectsWithBox(data.getBoundingBox().grow(0.2f,1f,0.2f))) {
                onHalfBlock = true;
            }

            if (BlockUtils.isIce(block) && playerBox.subtract(0, 1f, 0, 0, 0.5f, 0).collidesVertically(bb)) {
                onIce = true;
            }

            if (bb.collidesHorizontally(playerBox.grow((ProtocolVersion.getGameVersion().isOrAbove(ProtocolVersion.V1_13) ? 0.05f : 0), 0, (ProtocolVersion.getGameVersion().isOrAbove(ProtocolVersion.V1_13) ? 0.05f : 0)))) {
                collidesHorizontally = true;
            }

            if (bb.collidesVertically(playerBox.subtract(0, 0.1f, 0, 0, 0, 0)) && block.getType().toString().contains("SLIME")) {
                onSlime = true;
            }

            if (block.getType().toString().contains("SOUL") && bb.intersectsWithBox(getData().getBoundingBox().subtract(0, 0.01f, 0, 0, 1.4f, 0).shrink(0.2f, 0, 0.2f))) {
                onSoulSand = true;
            }

            if (BlockUtils.isClimbableBlock(block) && bb.intersectsWithBox(data.getBoundingBox().grow(0.1f, 0.1f, 0.1f))) {
                onClimbable = true;
            }

            if (!isEntity) {
                if (bb.intersectsWithBox(data.getBoundingBox().grow(1.0f, 0, 1.0f).shrink(0, 0.01f, 0))) {
                    blocksNear = true;
                }
                if (bb.intersectsWithBox(data.getBoundingBox().grow(1.5f, 1.5f, 1.5f))) {
                    blocksAround = true;
                }
            }
        } else {
            if (BlockUtils.isLiquid(block) && playerBox.grow(0.2f, 0.1f, 0.2f).collides(bb)) {
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