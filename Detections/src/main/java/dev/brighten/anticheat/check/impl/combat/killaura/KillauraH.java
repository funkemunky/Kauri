package dev.brighten.anticheat.check.impl.combat.killaura;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.TickTimer;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.AxisAlignedBB;
import dev.brighten.anticheat.utils.Vec3D;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;
import lombok.Getter;

import java.util.List;

@CheckInfo(name = "Killaura (H)", description = "Checks for weird misses", 
        devStage = DevStage.BETA, checkType = CheckType.KILLAURA)
public class KillauraH extends Check {

    @Getter
    private final List<SimpleCollisionBox> targetLocations = new EvictingList<>(6);
    private boolean didUse, didArm;
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
           if(targetLocations.size() == 0 || data.target == null) {
               debug("Null target");
               break check;
           }

           if(didArm && !didUse) {
               debug("%s: Reset Flying", tick);
               KLocation origin = data.playerInfo.to.clone(), forigin = data.playerInfo.from.clone();

               origin.y+= data.playerInfo.sneaking ? 1.54f : 1.62f;
               forigin.y+= data.playerInfo.lsneaking ? 1.54f : 1.62f;

               boolean missed = false;
               for (SimpleCollisionBox targetHitbox : targetLocations) {
                   AxisAlignedBB targetBox = new AxisAlignedBB(targetHitbox);

                   Vec3D intersection = targetBox.rayTrace(origin.toVector(),
                           MathUtils.getDirection(origin), 2.9), intersection2 =
                           targetBox.rayTrace(forigin.toVector(), MathUtils.getDirection(forigin), 2.9);

                   if(intersection == null
                           || intersection2 == null) {
                       missed = true;
                       debug("missed: %.3f, %.3f, %.3f", targetBox.minX, targetBox.minY, targetBox.minZ);
                   } else debug("Did not miss: %.3f, %.3f, %.3f",
                           targetBox.minX, targetBox.minY, targetBox.minZ);
               }

               if(!missed) {
                   vl++;
                   if(vl > 2) {
                       flag(120, "true;false");
                   }
               } else if(lastAlert.isPassed(120)) vl = 0;
           }
       }

       lastFlying.reset();
       didUse = didArm = false;
    }
}
