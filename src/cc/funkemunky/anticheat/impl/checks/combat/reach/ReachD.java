package cc.funkemunky.anticheat.impl.checks.combat.reach;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.utils.CustomLocation;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.PastLocation;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.utils.BoundingBox;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.math.RayTrace;
import com.google.common.collect.Lists;
import lombok.val;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

import java.util.List;

@Packets(packets = {
        Packet.Client.USE_ENTITY,
        Packet.Client.FLYING,
        Packet.Client.POSITION,
        Packet.Client.POSITION_LOOK,
        Packet.Client.LOOK,
        Packet.Client.LEGACY_POSITION,
        Packet.Client.LEGACY_POSITION_LOOK,
        Packet.Client.LEGACY_LOOK})
public class ReachD extends Check {

    public ReachD(String name, CancelType cancelType, int maxVL) {
        super(name, cancelType, maxVL);
    }

    @Setting
    private long pingRange = 100;

    @Setting
    private float maxReach = 3.0f;

    @Setting
    private int maxVL = 5;

    private LivingEntity target;
    private boolean attacked, cancel = false;
    private double vl;

    private PastLocation targetLocs = new PastLocation();

    @Override
    public Object onPacket(Object packet, String packetType, long timeStamp) {
        if(packetType.equals(Packet.Client.USE_ENTITY)) {
            WrappedInUseEntityPacket use = new WrappedInUseEntityPacket(packet, getData().getPlayer());

            if(use.getAction().equals(WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK)) {
                attacked = true;
                target = (LivingEntity) use.getEntity();

                if(cancel) {
                    use.setCancelled(true);
                    cancel = false;
                    return use;
                }
            }
        } else if(target != null) {
            if (attacked) {
                val entityData = Kauri.getInstance().getDataManager().getPlayerData(target.getUniqueId());

                if(entityData == null) {
                    targetLocs.addLocation(new CustomLocation(target.getLocation()));
                }

                WrappedInFlyingPacket flying = new WrappedInFlyingPacket(packet, getData().getPlayer());

                val origin = getData().getMovementProcessor().getTo().clone().toLocation(flying.getPlayer().getWorld()).add(0, 1.53f, 0);

                RayTrace trace = new RayTrace(origin.toVector(), origin.getDirection());

                List<Vector> vecs = trace.traverse( ((entityData != null ? entityData.getMovementProcessor().getTo().toVector() : target.getLocation().toVector()).distance(origin.toVector()) - 0.3) / 2, (entityData != null ? entityData.getMovementProcessor().getTo().toVector() : target.getLocation().toVector()).distance(origin.toVector()), 0.025);

                List<BoundingBox> entityBoxes = Lists.newArrayList();

                if(entityData == null) {
                    targetLocs.getEstimatedLocation(getData().getTransPing(), pingRange + MathUtils.getDelta(getData().getTransPing(), getData().getLastTransPing()))
                            .forEach(loc -> entityBoxes.add(getHitbox(loc)));
                } else {
                    entityData.getMovementProcessor().getPastLocation()
                            .getEstimatedLocation(getData().getTransPing(), pingRange + MathUtils.getDelta(getData().getTransPing(), getData().getLastTransPing()))
                            .forEach(loc -> entityBoxes.add(getHitbox(loc)));
                }

                List<Vector> finalVecs = Lists.newArrayList();

                vecs.stream().filter(vec -> entityBoxes.stream().anyMatch(box -> box.collides(vec))).forEach(finalVecs::add);

                double calculatedReach = 0;
                int collided = 0;
                for (Vector vec : finalVecs) {
                    double reach = origin.toVector().distance(vec);

                    calculatedReach = calculatedReach > 0 ? Math.min(calculatedReach, reach) : reach;
                    collided++;
                }

                if (collided > 0) {
                    if (calculatedReach > maxReach) {
                        if (vl++ > maxVL) {
                            flag(calculatedReach + ">-" + maxReach, false, true);
                        }
                        cancel = true;
                    } else {
                        vl = vl > 0 ? 0.25 : 0;
                    }

                    debug("VL: " + vl + "/" + maxVL + " REACH: " + calculatedReach + " COLLIDED: " + collided + " MAX: " + maxReach + " RANGE: " + pingRange);
                }

                attacked = false;
            }
        }
        return packet;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }

    private BoundingBox getHitbox(CustomLocation l) {
        return new BoundingBox(0,0,0,0,0,0).add((float) l.getX(), (float) l.getY(), (float) l.getZ()).grow(.4f, 0, .4f)
                .add(0,0,0,0, 1.85f, 0);
    }
}
