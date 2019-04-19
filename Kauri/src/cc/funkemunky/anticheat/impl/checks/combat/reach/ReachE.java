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
            long range = 50L + Math.abs(this.getData().getTransPing() - this.getData().getLastTransPing());
            val location = getData().getEntityPastLocation().getEstimatedLocation(getData().getTransPing(), range);
            val to = getData().getMovementProcessor().getTo().toLocation(target.getWorld()).clone().add(0, 1.54f, 0);
            val trace = new RayTrace(to.toVector(), to.getDirection());

            val traverse = trace.traverse(target.getLocation().distance(to) + 0.4, 0.05);
            float distance = (float)traverse.stream().filter((vec) -> {
                return location.stream().anyMatch((loc) -> {
                    return this.getHitbox(target, loc).intersectsWithBox(vec);
                });
            }).mapToDouble((vec) -> vec.distance(to.toVector()) + 0.05f).min().orElse(0.0D);

            if(distance <= 0.2) {
                return;
            }

            if(distance > 3.0F) {
                if(vl++ > 6.0F) {
                    this.flag("reach=" + distance, true, true);
                }
            } else vl-= vl > 0 ? 0.25 : 0;

            this.debug("distance=" + distance + " vl=" + this.vl + " range=" + range);
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }

    private BoundingBox getHitbox(LivingEntity entity, CustomLocation l) {
        Vector dimensions = (Vector)MiscUtils.entityDimensions.getOrDefault(entity.getType(), new Vector(0.35F, 1.85F, 0.35F));
        return (new BoundingBox(l.toVector(), l.toVector())).grow(0.15F, 0.15F, 0.15F).grow((float)dimensions.getX(), 0.0F, (float)dimensions.getZ()).add(0.0F, 0.0F, 0.0F, 0.0F, (float)dimensions.getY(), 0.0F);
    }
}
