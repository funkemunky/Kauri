package cc.funkemunky.anticheat.impl.checks.combat.hitboxes;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.CustomLocation;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.BoundingBox;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.math.RayTrace;
import lombok.val;
import lombok.var;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

import java.util.Objects;

@CheckInfo(name = "HitBox (Type B)", description = "Checks to make sure a player is looking at the hitbox of the entity when attacking.", type = CheckType.COMBAT, cancelType = CancelType.COMBAT)
@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.POSITION, Packet.Client.LOOK, Packet.Client.FLYING, Packet.Client.LEGACY_LOOK, Packet.Client.LEGACY_POSITION, Packet.Client.LEGACY_POSITION_LOOK})
@Init
public class HitBoxB extends Check {

    private float vl;

    @Setting(name = "threshold.vl.max")
    private int maxVL = 6;

    @Setting(name = "threshold.vl.subtract")
    private float subtract = 0.5f;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        if(getData().getLastAttack().hasNotPassed(0) && getData().getTarget() != null) {
            val target = getData().getTarget();
            val origin = move.getTo().toLocation(target.getWorld()).add(0, 1.54, 0);
            val trace = new RayTrace(origin.toVector(), origin.getDirection());
            val range = 100 + Math.abs(getData().getTransPing() - getData().getLastTransPing());
            val estimated = getData().getEntityPastLocation().getEstimatedLocation(getData().getTransPing(), range);
            val traverse = trace.traverse(target.getLocation().distance(origin), 0.2f);
            val count = traverse.stream()
                    .filter(vec -> estimated.stream().anyMatch(loc -> {
                        BoundingBox hitbox = getHitbox(target, loc);

                        if(hitbox != null) {
                            hitbox = move.getYawDelta() > 1.2 || move.getPitchDelta() > 2 ? hitbox.grow(0.25f, 0.25f, 0.25f) : hitbox;
                            return hitbox.intersectsWithBox(vec);
                        }
                        return false;
                    }))
                    .count();

            if(count == 0) {
                if (vl++ > maxVL) {
                    flag("count=0", true, true);
                }
            } else vl-= vl > 0 ? subtract : 0;

            debug("count=" + count + " range=" + range + " vl=" + vl);
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }

    private BoundingBox getHitbox(LivingEntity entity, CustomLocation l) {
        Vector dimensions = MiscUtils.entityDimensions.getOrDefault(entity.getType(), null);
        return dimensions == null ? null : new BoundingBox(l.toVector(), l.toVector()).grow((float) dimensions.getX(), 0, (float) dimensions.getZ()).add(0,0,0,0, (float) dimensions.getY(),0);
    }
}
