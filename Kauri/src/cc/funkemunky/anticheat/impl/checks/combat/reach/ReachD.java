package cc.funkemunky.anticheat.impl.checks.combat.reach;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.CustomLocation;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.PastLocation;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedPacketPlayOutWorldParticle;
import cc.funkemunky.api.tinyprotocol.packet.types.WrappedEnumParticle;
import cc.funkemunky.api.utils.BoundingBox;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.math.RayTrace;
import com.google.common.collect.Lists;
import lombok.val;
import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Packets(packets = {
        Packet.Client.ARM_ANIMATION,
        Packet.Client.USE_ENTITY,
        Packet.Client.FLYING,
        Packet.Client.POSITION,
        Packet.Client.POSITION_LOOK,
        Packet.Client.LOOK,
        Packet.Client.LEGACY_POSITION,
        Packet.Client.LEGACY_POSITION_LOOK,
        Packet.Client.LEGACY_LOOK})
public class ReachD extends Check {

    private LivingEntity target;
    private PastLocation targetLocs = new PastLocation();

    public ReachD(String name, CheckType type, CancelType cancelType, int maxVL) {
        super(name, type, cancelType, maxVL);

        new BukkitRunnable() {
            public void run() {
                if(getData() != null && target != null && (!(target instanceof Player) || !Kauri.getInstance().getDataManager().getDataObjects().containsKey(target.getUniqueId()))) {
                    targetLocs.addLocation(new CustomLocation(target.getLocation()));
                }
            }
        }.runTaskTimer(Kauri.getInstance(), 20L, 1L);
    }

    @Setting(name = "pingRange")
    private long pingRange = 150;

    @Setting(name = "maxReach")
    private float maxReach = 3.0f;

    @Setting
    private int maxVL = 5;

    private boolean attacked;
    private double vl;


    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(packetType.equals(Packet.Client.USE_ENTITY)) {
            WrappedInUseEntityPacket use = new WrappedInUseEntityPacket(packet, getData().getPlayer());

            if(use.getAction().equals(WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK)) {
                attacked = true;
                target = (LivingEntity) use.getEntity();
            }
        } else if(packetType.equals(Packet.Client.ARM_ANIMATION)) {
            vl -= vl > 0 ? 0.01 : 0;
        } else if(target != null && getData().getPlayer().getGameMode().equals(GameMode.SURVIVAL)) {
            if (attacked) {
                val entityData = Kauri.getInstance().getDataManager().getPlayerData(target.getUniqueId());

                if(getData().getPing() > 400 || (entityData != null && getData().getPing() > 400)) {
                    return;
                }

                WrappedInFlyingPacket flying = new WrappedInFlyingPacket(packet, getData().getPlayer());

                val origin = getData().getMovementProcessor().getTo().clone().toLocation(flying.getPlayer().getWorld()).add(0, 1.53, 0);

                RayTrace trace = new RayTrace(origin.toVector(), origin.getDirection());

                List<Vector> vecs = trace.traverse(target.getEyeLocation().distance(origin), 0.1);

                List<BoundingBox> entityBoxes = new CopyOnWriteArrayList<>();

                if(entityData == null) {
                    targetLocs.getEstimatedLocation(getData().getTransPing(), pingRange + MathUtils.getDelta(getData().getTransPing(), getData().getLastTransPing()))
                            .forEach(loc -> entityBoxes.add(getHitbox(target, loc)));
                } else {
                    entityData.getMovementProcessor().getPastLocation()
                            .getEstimatedLocation(getData().getTransPing(), pingRange + MathUtils.getDelta(getData().getTransPing(), getData().getLastTransPing()))
                            .forEach(loc -> entityBoxes.add(getHitbox(target, loc)));
                }

                double calculatedReach = 0;
                int collided = 0;
                
                vecs.sort(Comparator.comparingDouble(vec -> vec.distance(origin.toVector())));

                List<Vector> finalVecs = new ArrayList<>();
                vecs.stream().filter(vec -> entityBoxes.stream().anyMatch(box -> box.collides(vec))).forEach(finalVecs::add);

                for(Vector vec : finalVecs) {
                    double reach = origin.toVector().distance(vec);
                    calculatedReach = calculatedReach == 0 ? reach : Math.min(reach, calculatedReach);
                   // WrappedPacketPlayOutWorldParticle particle = new WrappedPacketPlayOutWorldParticle(WrappedEnumParticle.FIREWORKS_SPARK, true, (float) vec.getX(), (float) vec.getY(), (float) vec.getZ(), 0, 0, 0, 0, 1);

                    //particle.sendPacket(getData().getPlayer());
                    collided++;
                }

                if (collided > 1) {
                    if (calculatedReach > maxReach + 0.2) {
                        if (vl++ > maxVL) {
                            flag(calculatedReach + ">-" + maxReach, false, true);
                        }

                        debug(Color.Green + "REACH: " + calculatedReach);
                    } else {
                        vl -= vl > 0 ? 0.2 : 0;
                    }

                    debug("VL: " + vl + "/" + maxVL + " REACH: " + calculatedReach + " COLLIDED: " + collided + " MAX: " + maxReach + " RANGE: " + pingRange);
                }

                attacked = false;
            }
        }
        return;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }

    private BoundingBox getHitbox(LivingEntity entity, CustomLocation l) {
        val dimensions = MiscUtils.entityDimensions.getOrDefault(entity.getType(), new Vector(0.35f,1.85f,0.35f));

        return new BoundingBox(l.toVector(), l.toVector()).grow(.15f, .15f, .15f).grow((float) dimensions.getX(), 0, (float) dimensions.getZ()).add(0,0,0,0, (float) dimensions.getY(),0);
    }
}
