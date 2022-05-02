package dev.brighten.anticheat.check.impl.combat.killaura;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.world.CollisionBox;
import cc.funkemunky.api.utils.world.EntityData;
import cc.funkemunky.api.utils.world.types.RayCollision;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;
import org.bukkit.util.Vector;

@CheckInfo(name = "Killaura (A)", description = "Checks for block collisions on player hits.",
        checkType = CheckType.KILLAURA, devStage = DevStage.BETA, punishVL = 8)
@Cancellable(cancelType = CancelType.ATTACK)
public class KillauraA extends Check {

    private int buffer;

    @Packet
    public void onUse(WrappedInUseEntityPacket packet) {
        if(data.target == null
                || packet.getAction() != WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK)
            return;

        //We can't run this check if we have no block boxes to check!
        if(data.getLookingAtBoxes().size() == 0) {
            debug("No block boxes to look at");
            buffer = 0; //Resetting buffer
            return;
        }

        //Get a single target box.
        SimpleCollisionBox targetBox = (SimpleCollisionBox) EntityData.getEntityBox(data.target.getLocation(), data.target);

        if(targetBox == null) return;

        KLocation origin = data.playerInfo.to.clone();

        origin.y+= data.playerInfo.sneaking ? 1.54f : 1.62f;

        RayCollision ray = new RayCollision(origin.toVector(), MathUtils.getDirection(origin));

        Vector targetPoint = ray.collisionPoint(targetBox);
        //If the ray isn't collided, we might as well not run this check. Just a simple boxes on array check
        if(targetPoint == null) return;

        double dist = origin.toVector().distanceSquared(targetPoint);

        boolean rayCollidedOnBlock = false;

        synchronized (data.getLookingAtBoxes()) {
            for (CollisionBox lookingAtBox : data.getLookingAtBoxes()) {
                if((lookingAtBox instanceof SimpleCollisionBox)) {
                    SimpleCollisionBox box = (SimpleCollisionBox) lookingAtBox;
                    
                    if(box.xMin % 1 != 0 || box.yMin % 1 != 0 || box.zMin % 1 != 0 
                            || box.xMax % 1 != 0 || box.yMax % 1 != 0 || box.zMax % 1 != 0)
                        continue;

                    Vector point = ray.collisionPoint(box.copy().shrink(0.005f, 0.005f, 0.005f));

                    if (point != null && origin.toVector().distanceSquared(point) < dist - 0.2) {
                        rayCollidedOnBlock = true;
                        break;
                    }
                }
            }

            if(rayCollidedOnBlock) {
                if(++buffer > 2) {
                    vl++;
                    flag("b=%s s=%s", buffer, data.getLookingAtBoxes().size());
                }
            } else if(buffer > 0) buffer--;
        }

        debug("b=%s collides=%s", buffer, rayCollidedOnBlock);
    }
}
