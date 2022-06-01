package dev.brighten.anticheat.check.impl.combat.killaura;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.TickTimer;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import cc.funkemunky.api.utils.world.EntityData;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.AxisAlignedBB;
import dev.brighten.anticheat.utils.EntityLocation;
import dev.brighten.anticheat.utils.Vec3D;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;
import lombok.Getter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@CheckInfo(name = "Killaura (H)", description = "Checks for weird misses", 
        devStage = DevStage.BETA, checkType = CheckType.KILLAURA)
public class KillauraH extends Check {

    @Getter
    private boolean didUse, didArm;
    private float buffer;
    private final TickTimer lastFlying = new TickTimer(5);

    @Packet
    public void onUse(WrappedInUseEntityPacket packet, int tick) {
        didUse = true;
        debug("%s: Did Use", tick);
    }

    @Packet
    public void onArmAnimation(WrappedInArmAnimationPacket packet, int tick) {
        didArm = true;
        debug("%s: Did Arm", tick);
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, int tick) {
       check: {
           if(lastFlying.hasPassed(1)) {
               didUse = didArm = false;
               break check;
           }

           if(data.target == null) {
               debug("Null target");
               break check;
           }

           Optional<EntityLocation> op = data.entityLocationProcessor.getEntityLocation(data.target);

           if(op.isPresent()) {
               EntityLocation entityLoc = op.get();

               if(entityLoc.interpolatedLocations.size() == 0) break check;

               double expander = data.playerVersion.isOrAbove(ProtocolVersion.V1_9) ? 0 : 0.1;
               List<SimpleCollisionBox> targetLocations = entityLoc.interpolatedLocations.stream()
                       .map(l -> ((SimpleCollisionBox)EntityData.getEntityBox(l, data.target)).expand(expander))
                       .collect(Collectors.toList());

               if (didArm && !didUse) {
                   debug("%s: Reset Flying", tick);
                   KLocation origin = data.playerInfo.to.clone(), forigin = data.playerInfo.from.clone();

                   origin.y += data.playerInfo.sneaking ? 1.54f : 1.62f;
                   forigin.y += data.playerInfo.lsneaking ? 1.54f : 1.62f;

                   boolean missed = false;
                   for (SimpleCollisionBox targetHitbox : targetLocations) {
                       AxisAlignedBB targetBox = new AxisAlignedBB(targetHitbox);

                       Vec3D intersection = targetBox.rayTrace(origin.toVector(),
                               MathUtils.getDirection(origin), 2.95), intersection2 =
                               targetBox.rayTrace(forigin.toVector(), MathUtils.getDirection(forigin),2.95);

                       if (intersection == null
                               || intersection2 == null) {
                           missed = true;
                           debug("missed: %.3f, %.3f, %.3f", targetBox.minX, targetBox.minY, targetBox.minZ);
                       } else debug("Did not miss: %.3f, %.3f, %.3f",
                               targetBox.minX, targetBox.minY, targetBox.minZ);
                   }

                   if (!missed) {
                       buffer++;
                       debug(Color.Green + "Didn't miss!");
                       if (buffer > 1) {
                           vl++;
                           flag(120, "true;false");
                       }
                   } else if(buffer > 0) buffer-= 0.5f;
               } else if(buffer > 0) buffer-= 0.005f;
           }
       }

       lastFlying.reset();
       didUse = didArm = false;
    }
}
