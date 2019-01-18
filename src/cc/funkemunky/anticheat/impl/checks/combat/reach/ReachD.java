package cc.funkemunky.anticheat.impl.checks.combat.reach;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.utils.CustomLocation;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.utils.BoundingBox;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.math.RayTrace;
import com.google.common.collect.Lists;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
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
    private long pingRange = 185;

    @Setting
    private float maxReach = 3.02f;

    @Setting
    private int maxVL = 4;

    private Player target;
    private boolean attacked;
    private int vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(packetType.equals(Packet.Client.USE_ENTITY)) {
            WrappedInUseEntityPacket use = new WrappedInUseEntityPacket(packet, getData().getPlayer());

            if(use.getAction().equals(WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK) && use.getEntity() instanceof Player) {
                attacked = true;
                target = (Player) use.getEntity();
            }
        } else if(attacked) {
            WrappedInFlyingPacket flying = new WrappedInFlyingPacket(packet, getData().getPlayer());

            if(target == null) return;

            val entityData = Kauri.getInstance().getDataManager().getPlayerData(target.getUniqueId());

            if(entityData == null || entityData.getTransPing() > 450 || getData().getTransPing() > 450) return;

            val origin = getData().getMovementProcessor().getTo().clone().toLocation(flying.getPlayer().getWorld()).add(0, 1.53f, 0);

            RayTrace trace = new RayTrace(origin.toVector(), origin.getDirection());

            List<Vector> vecs = trace.traverse(entityData.getMovementProcessor().getTo().toVector().distance(origin.toVector()), 0.05);

            List<BoundingBox> entityBoxes = Lists.newArrayList();

            entityData.getMovementProcessor().getPastLocation()
                    .getEstimatedLocation(getData().getTransPing(), pingRange + MathUtils.getDelta(getData().getTransPing(), getData().getLastTransPing()))
                    .forEach(loc -> entityBoxes.add(getHitbox(loc)));

            List<Vector> finalVecs = Lists.newArrayList();

            vecs.stream().filter(vec -> entityBoxes.stream().anyMatch(box -> box.collides(vec))).forEach(finalVecs::add);

            double calculatedReach = 0;
            int collided = 0;
            for(Vector vec : finalVecs) {
                double reach = origin.toVector().distance(vec);

                calculatedReach = calculatedReach > 0 ? Math.min(calculatedReach, reach) : reach;
                collided++;
            }

            if(collided > 0) {
                if(calculatedReach > maxReach) {
                    if(vl++ > maxVL) {
                        flag(calculatedReach + ">-" + maxReach, false, true);
                    }
                } else {
                    vl = vl > 0 ? 1 : 0;
                }

                Bukkit.broadcastMessage("REACH: " + calculatedReach + " COLLIDED: " + collided);
            }

            attacked = false;
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }

    private BoundingBox getHitbox(CustomLocation l) {
        return new BoundingBox(0,0,0,0,0,0).add((float) l.getX(), (float) l.getY(), (float) l.getZ()).grow(.3f, 0, .3f)
                .add(0,0,0,0, 1.85f, 0);
    }
}
