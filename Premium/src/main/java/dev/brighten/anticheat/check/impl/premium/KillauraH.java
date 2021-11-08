package dev.brighten.anticheat.check.impl.premium;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.world.CollisionBox;
import cc.funkemunky.api.utils.world.EntityData;
import cc.funkemunky.api.utils.world.types.RayCollision;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import lombok.Setter;
import lombok.val;

@CheckInfo(name = "Killaura (H)", description = "Checks for weird misses")
public class KillauraH extends Check {

    @Setter
    private KLocation targetLocation;
    private boolean didUse, didArm;

    @Packet
    public void onUse(WrappedInUseEntityPacket packet) {
        didUse = true;
    }

    @Packet
    public void onArmAnimation(WrappedInArmAnimationPacket packet) {
        didArm = true;
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
       check: {
           if(targetLocation == null || data.target == null) break check;

           if(didArm && !didUse) {
               KLocation origin = data.playerInfo.to.clone();

               origin.y+= data.playerInfo.sneaking ? 1.54f : 1.62f;

               SimpleCollisionBox targetHitbox = (SimpleCollisionBox) EntityData
                       .getEntityBox(targetLocation, data.target);

               RayCollision collision = new RayCollision(origin.toVector(), MathUtils.getDirection(origin));

               val intersction = collision.collisionPoint(targetHitbox);

               if(intersction != null && origin.toVector().distanceSquared(intersction) <= 9) {
                   vl++;
                   flag("weird miss");
               }
           }
       }

       didUse = didArm = false;
    }
}
