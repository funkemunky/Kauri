package dev.brighten.anticheat.utils;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.tinyprotocol.packet.types.WrappedEnumParticle;
import cc.funkemunky.api.utils.BlockUtils;
import cc.funkemunky.api.utils.BoundingBox;
import cc.funkemunky.api.utils.MiscUtils;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.data.ObjectData;
import org.bukkit.block.Block;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CollisionHandler {
    private ObjectData data;
    public boolean collidingGround, onGround, nearGround, collidesHorizontally, collidesVertically;
    public List<Block> blocksUnderPlayer = new ArrayList<>();
    public List<Map.Entry<Block, BoundingBox>> boxesColliding = new ArrayList<>(), allBlocks = new ArrayList<>();

    public CollisionHandler(ObjectData data) {
        this.data = data;
    }

    public void onCollide(Block block, BoundingBox box, boolean entity) {
        if(data.box.collides(box)) {
            boxesColliding.add(new AbstractMap.SimpleEntry<>((entity ? null : block), box));
        }

        if(entity || BlockUtils.isSolid(block)) {
            if(data.box.subtract(0,0,0,0,0.5f,0).collidesVertically(box)
                    || box.collidesVertically(data.box
                    .subtract(0, 0.1f, 0, 0, 1, 0))) {
                onGround = nearGround = true;

                if(!entity) {
                    blocksUnderPlayer.add(block);
                }
            } else if(box.collidesVertically(data.box
                    .subtract(0, 1, 0,0,1,0))) {
                nearGround = true;
            }

            if(data.box.collidesHorizontally(box)) {
                collidesHorizontally = true;
            }

            if(data.box.collidesVertically(box)) {
                collidesVertically = true;
            }

            if(Atlas.getInstance().getCurrentTicks() % 2 == 0) {
                Kauri.INSTANCE.executor.execute(() -> MiscUtils.createParticlesForBoundingBox(
                        data.getPlayer(),
                        box,
                        WrappedEnumParticle.CRIT_MAGIC,
                        0.2f));
            }

            if(!entity) {
                allBlocks.add(new AbstractMap.SimpleEntry<>(block, box));
            }
        }
    }
}
