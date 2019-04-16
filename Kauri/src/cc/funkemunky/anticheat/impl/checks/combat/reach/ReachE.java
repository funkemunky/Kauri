package cc.funkemunky.anticheat.impl.checks.combat.reach;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.CustomLocation;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.BoundingBox;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.math.RayTrace;
import lombok.val;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

@CheckInfo(name = "Reach (Type E)", description = "test", type = CheckType.REACH, cancelType = CancelType.COMBAT)
@Packets(packets = {Packet.Client.POSITION, Packet.Client.POSITION_LOOK, Packet.Client.LEGACY_POSITION_LOOK, Packet.Client.LEGACY_POSITION})
@Init
public class ReachE extends Check {

    private float vl = 0;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val target = getData().getTarget();

        if(target != null && getData().getLastAttack().hasNotPassed(1)) {
            val location = getData().getEntityPastLocation().getEstimatedLocation(getData().getTransPing(), 100L);
            val to = getData().getMovementProcessor().getTo().toLocation(target.getWorld()).clone().add(0, 1.54f, 0);
            val player = getData().getPlayer();

            RayTrace trace = new RayTrace(to.toVector(), to.getDirection());

            val traverse = trace.traverse(target.getLocation().distance(to) + 0.4, 0.05);
            val dimensions = MiscUtils.entityDimensions.getOrDefault(target.getType(), new Vector(0.3, 1.8, 0.3));

            val distance = (float) traverse.stream().filter(vec -> location.stream().anyMatch(loc -> {
                return getHitbox(target, loc).intersectsWithBox(vec);
            })).mapToDouble(vec -> vec.distance(to.toVector()) + 0.1f).min().orElse(0);

            if(distance <= 0.2) return;

            if(distance > 3) {
                if(vl++ > 6) {
                    flag("reach=" + distance, true, true);
                }
            } else vl-= vl > 0 ? 0.1 : 0;

            debug("DISTANCE: " + distance + " VL: " + vl);
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
