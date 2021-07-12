package dev.brighten.anticheat.check.impl.combat.killaura;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.math.cond.MaxInteger;
import cc.funkemunky.api.utils.world.CollisionBox;
import cc.funkemunky.api.utils.world.EntityData;
import cc.funkemunky.api.utils.world.types.RayCollision;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.MiscUtils;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.Optional;

@CheckInfo(name = "Killaura (A)", description = "Checks for block collisions on player hits.",
        checkType = CheckType.KILLAURA, developer = true)
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
        CollisionBox targetBox = EntityData.getEntityBox(data.target.getLocation(), data.target);

        if(targetBox == null) return;

        KLocation origin = data.playerInfo.to.clone();

        origin.y+= data.playerInfo.sneaking ? 1.54f : 1.62f;

        RayCollision ray = new RayCollision(origin.toVector(), MathUtils.getDirection(origin));

        //If the ray isn't collided, we might as well not run this check. Just a simple boxes on array check
        if(!ray.isCollided(targetBox)) return;

        boolean rayCollidedOnBlock = false;

        synchronized (data.getLookingAtBoxes()) {
            for (CollisionBox lookingAtBox : data.getLookingAtBoxes()) {
                if(ray.isCollided(lookingAtBox.copy().shrink(0.25,0.25,0.25))) {
                    rayCollidedOnBlock = true;
                    break;
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
