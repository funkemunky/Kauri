package dev.brighten.anticheat.data.classes;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.reflection.MinecraftReflection;
import cc.funkemunky.api.tinyprotocol.packet.types.WrappedEnumParticle;
import cc.funkemunky.api.utils.BlockUtils;
import cc.funkemunky.api.utils.BoundingBox;
import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.ReflectionsUtil;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.processing.EntityProcessor;
import dev.brighten.anticheat.utils.CollisionHandler;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BlockInformation {
    private ObjectData objectData;
    public boolean onClimbable, onSlab, onStairs, onHalfBlock, inLiquid, inLava, inWater, inWeb, onSlime, onIce,
            onSoulSand, blocksAbove, collidesVertically, collidesHorizontally;

    public BlockInformation(ObjectData objectData) {
        this.objectData = objectData;
    }

    public void runCollisionCheck() {
        CollisionHandler handler = new CollisionHandler(objectData);

        List<BoundingBox> boxes = Atlas.getInstance().getBlockBoxManager().getBlockBox().getCollidingBoxes(objectData.getPlayer().getWorld(), objectData.box.grow(1,1,1));

        //Running block checking;
        boxes.parallelStream().forEach(box -> {
            Block block = BlockUtils.getBlock(box.getMinimum().toLocation(objectData.getPlayer().getWorld()));

            if(block != null) {
                handler.onCollide(block, box, false);
            }
        });

            //Running entity boundingBox check.
        EntityProcessor.vehicles.get(objectData.getPlayer().getWorld().getUID())
                    .stream()
                    .filter(entity -> entity.getLocation().distance(objectData.getPlayer().getLocation()) < 1.5)
                    .map(entity -> ReflectionsUtil.toBoundingBox(ReflectionsUtil.getBoundingBox(entity)))
                    .forEach(box -> handler.onCollide(null, box, true));

        objectData.playerInfo.serverGround = handler.onGround;
        objectData.playerInfo.nearGround = handler.nearGround;
        objectData.playerInfo.collided =
                (objectData.playerInfo.collidesHorizontally = handler.collidesHorizontally)
                        || (objectData.playerInfo.collidesVertically = handler.collidesVertically);
        onSlab = handler.onSlab;
        onStairs = handler.onStairs;
        onHalfBlock = handler.onHalfBlock;
        inLiquid = handler.inLiquid;
        inWeb = handler.inWeb;
        onSlime = handler.onSlime;
        onIce = handler.onIce;
        onSoulSand = handler.onSoulSand;
        inLava = handler.inLava;
        inWater = handler.inWater;
        blocksAbove = handler.blocksAbove;
        collidesHorizontally = handler.collidesHorizontally;
        collidesVertically = handler.collidesVertically;
    }
}
