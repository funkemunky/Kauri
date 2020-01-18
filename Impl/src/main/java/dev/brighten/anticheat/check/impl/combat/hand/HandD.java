package dev.brighten.anticheat.check.impl.combat.hand;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.BoundingBox;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.world.types.RayCollision;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.api.check.CheckType;
import lombok.val;
import lombok.var;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

@CheckInfo(name = "Hand (D)", description = "Checks for block collisions on player hits.", checkType = CheckType.HAND,
        developer = true)
@Cancellable(cancelType = CancelType.INTERACT)
public class HandD extends Check {

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(data.target != null && !data.playerInfo.generalCancel
                && data.playerInfo.lastAttack.hasNotPassed(0)) {
            val locs = data.pastLocation.getEstimatedLocation(0, 50L);

            int size = 0;
            int collided = 0;
            int total = 0;
            for (KLocation loc : locs) {
                val to = loc.toLocation(data.getPlayer().getWorld())
                        .add(0, data.playerInfo.sneaking ? 1.54 : 1.62, 0);
                RayCollision collision = new RayCollision(to.toVector(), to.getDirection());

                var vec = collision.collisionPoint(getHitbox
                        (new KLocation(data.target.getLocation()), data.target.getType()).toCollisionBox());
                var distance = 4f;
                if(vec != null) {
                    distance = Math.min((float)to.toVector().distance(vec), 4f);
                } else {
                    size++;
                    continue;
                }

                if(distance < 0.8) {
                    size++;
                    continue;
                }

                val boxes = collision
                        .boxesOnRay(to.getWorld(), distance < 1.5 ? Math.min(distance / 2, .25) : distance - 1);

                if(boxes.size() > 0) {
                    collided++;
                    total+= boxes.size();
                }
                size++;
            }

            if(collided >= size && data.playerInfo.lastBrokenBlock.hasPassed(10) && size > 1) {
                if(vl++ > 5) {
                    flag("collided=%1/%2 total=%3 lagging=%4", collided, size, total, data.lagInfo.lagging);
                }
            } else vl-= vl > 0 ? 0.2f : 0;
            debug("collided=%1/%2 total=%3 lagging=%4 vl=%5", collided, size, total, data.lagInfo.lagging, vl);
        }
    }

    private static BoundingBox getHitbox(KLocation loc, EntityType type) {
        Vector bounds = MiscUtils.entityDimensions.get(type);

        BoundingBox box = new BoundingBox(loc.toVector(), loc.toVector())
                .grow((float)bounds.getX(), 0, (float)bounds.getZ())
                .add(0,0,0,0,(float)bounds.getY(),0)
                .grow(0.02f,0.02f,0.02f);

        if(ProtocolVersion.getGameVersion().isBelow(ProtocolVersion.V1_9)) {
            return box.grow(0.1f,0.1f,0.1f);
        }

        return box;
    }
}
