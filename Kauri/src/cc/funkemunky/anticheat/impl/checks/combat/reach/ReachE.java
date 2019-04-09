package cc.funkemunky.anticheat.impl.checks.combat.reach;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.CustomLocation;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedPacketPlayOutWorldParticle;
import cc.funkemunky.api.tinyprotocol.packet.types.WrappedEnumParticle;
import cc.funkemunky.api.utils.BoundingBox;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.math.RayTrace;
import lombok.val;
import org.bukkit.GameMode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Packets(packets = {
        Packet.Client.ARM_ANIMATION,
        Packet.Client.FLYING,
        Packet.Client.POSITION,
        Packet.Client.POSITION_LOOK,
        Packet.Client.LOOK,
        Packet.Client.LEGACY_POSITION,
        Packet.Client.LEGACY_POSITION_LOOK,
        Packet.Client.LEGACY_LOOK})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "Reach (Type E)", description = "A ray-tracing check but is less light, however it detects 3.1 reach very accurately.", type = CheckType.REACH, cancelType = CancelType.COMBAT, maxVL = 125)
public class ReachE extends Check {

    @Setting(name = "pingRange")
    private long pingRange = 50;

    @Setting(name = "theshold.reach")
    private float maxReach = 3.0f;

    @Setting(name = "threshold.vl.max")
    private int maxVL = 5;

    @Setting(name = "threshold.vl.nonBanMax")
    private double nonBanMaxVL = 2.0;

    @Setting(name = "threshold.vl.nonBanMult")
    private double nonBanMult = 5.0;

    @Setting(name = "threshold.vl.add")
    private double vlAdd = 1;

    @Setting(name = "threshold.vl.subtract.arm")
    private double armSubtract = 0.005;

    @Setting(name = "threshold.vl.subtract.belowThreshold")
    private double belowTSubtract = 0.1;

    @Setting(name = "threshold.vl.subtract.belowCollisionMin")
    private double belowCollisionSubtract = 0.1;

    @Setting(name = "threshold.collisionMin")
    private int collisionMin = 2;

    private double vl;


    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if (packetType.equals(Packet.Client.ARM_ANIMATION)) {
            vl -= vl > 0 ? armSubtract : 0;
        } else if (getData().getTarget() != null && getData().getTarget().getWorld().getUID().equals(getData().getPlayer().getWorld().getUID()) && getData().getLastAttack().hasNotPassed(0) && getData().getPlayer().getGameMode().equals(GameMode.SURVIVAL)) {
            val target = getData().getTarget();
            val entityData = Kauri.getInstance().getDataManager().getPlayerData(target.getUniqueId());

            if (getData().getPing() > 400 || (entityData != null && getData().getPing() > 400) || getData().isLagging()) {
                return;
            }

            WrappedInFlyingPacket flying = new WrappedInFlyingPacket(packet, getData().getPlayer());

            val origin = getData().getMovementProcessor().getTo().clone().toLocation(flying.getPlayer().getWorld()).add(0, 1.53, 0);

            RayTrace trace = new RayTrace(origin.toVector(), origin.getDirection());

            List<Vector> vecs = trace.traverse(target.getEyeLocation().distance(origin), 0.01f);

            List<BoundingBox> entityBoxes = new CopyOnWriteArrayList<>();

            if (entityData == null) {
                getData().getEntityPastLocation().getEstimatedLocation(getData().getTransPing(), Math.max(0, pingRange + (getData().getMovementProcessor().getDeltaXZ() < 0.23 ? -50 : 0)) + MathUtils.getDelta(getData().getTransPing(), getData().getLastTransPing()))
                        .forEach(loc -> entityBoxes.add(getHitbox(target, loc)));
            } else {
                entityData.getMovementProcessor().getPastLocation()
                        .getEstimatedLocation(getData().getTransPing(), getData().isLagging() ? pingRange + MathUtils.getDelta(getData().getTransPing(), getData().getLastTransPing()) : 50)
                        .forEach(loc -> entityBoxes.add(getHitbox(target, loc)));
            }

            double calculatedReach = 0;
            int collided = 0;

            vecs.sort(Comparator.comparingDouble(vec -> vec.distance(origin.toVector())));

            List<Vector> finalVecs = new ArrayList<>();
            vecs.stream().filter(vec -> entityBoxes.stream().anyMatch(box -> box.collides(vec))).forEach(finalVecs::add);

            for (Vector vec : finalVecs) {
                double reach = origin.toVector().distance(vec);
                if(reach <= 0.5) continue;

                calculatedReach = calculatedReach == 0 ? reach : Math.min(reach, calculatedReach);

                collided++;
            }

           if(collided > 0) {
               if (calculatedReach > maxReach + 0.051) {
                   if ((vl += (vlAdd + (calculatedReach > 3.2 ? 1 : 0))) > nonBanMaxVL) {
                       if (vl > maxVL) {
                           flag(calculatedReach + ">-" + maxReach, false, true);
                       } else {
                           flag(calculatedReach + ">-" + maxReach, false, false);
                       }
                   }

                   flag(calculatedReach + ">-" + maxReach + " (DEVELOPERS ONLY)", false, false);

                   debug(Color.Green + "REACH: " + calculatedReach);
               } else {
                   vl -= vl > 0 ? belowTSubtract : 0;
               }
           }

            debug("VL: " + vl + "/" + maxVL + " REACH: " + calculatedReach + " COLLIDED: " + collided + " MAX: " + maxReach + " RANGE: " + pingRange);
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }

    private BoundingBox getHitbox(LivingEntity entity, CustomLocation l) {
        val dimensions = MiscUtils.entityDimensions.getOrDefault(entity.getType(), new Vector(0.35f, 1.85f, 0.35f));

        return new BoundingBox(l.toVector(), l.toVector()).grow(0.1f, 0.1f, 0.1f).grow((float) dimensions.getX(), 0, (float) dimensions.getZ()).add(0, 0, 0, 0, (float) dimensions.getY(), 0);
    }
}
