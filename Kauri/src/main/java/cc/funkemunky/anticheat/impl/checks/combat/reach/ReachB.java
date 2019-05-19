package cc.funkemunky.anticheat.impl.checks.combat.reach;

import cc.funkemunky.anticheat.api.checks.*;
import cc.funkemunky.anticheat.api.utils.CustomLocation;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.BoundingBox;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.math.RayTrace;
import lombok.val;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

@CheckInfo(name = "Reach (Type B)", description = "A reach check idk", type = CheckType.REACH, cancelType = CancelType.COMBAT, developer = true)
@Packets(packets = {Packet.Client.USE_ENTITY, Packet.Client.ARM_ANIMATION})
@Init
public class ReachB extends Check {

    private double vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(packetType.equals(Packet.Client.USE_ENTITY)) {
            val move = getData().getMovementProcessor();

            val origin = move.getTo().toLocation(getData().getPlayer().getWorld()).add(0, 1.53f, 0);

            val trace = new RayTrace(origin.toVector(), origin.getDirection());

            val vectors = trace.traverse(3, 0.1);

            val pastLoc = getData().getEntityPastLocation().getEstimatedLocation(getData().getPing(), 150 + Math.abs(getData().getPing() - getData().getLastPing()));

            val doesMatch = vectors.stream().anyMatch(vec -> pastLoc.stream().anyMatch(loc -> getHitbox(getData().getTarget(), loc).intersectsWithBox(vec)));

            if(!doesMatch) {
                val reach = pastLoc.stream().mapToDouble(loc -> loc.toVector().add(new Vector(0, 1.53, 0)).distance(origin.toVector())).average().getAsDouble();

                if(vl++ > 8) {
                    flag("reach=" + MathUtils.round(reach, 2) + " vl=" + vl, true, true, AlertTier.HIGH);
                }
                debug("vl=" + vl  + " reach=" + reach);
            } else vl-= vl > 0 ? 0.5 : 0;
        } else vl-= vl > 0 ? 0.05 : 0;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }

    private BoundingBox getHitbox(LivingEntity entity, CustomLocation l) {
        Vector dimensions = MiscUtils.entityDimensions.getOrDefault(entity.getType(), new Vector(0.35F, 1.85F, 0.35F));
        return (new BoundingBox(l.toVector(), l.toVector())).grow(0.1F, 0.1F, 0.1F).grow((float)dimensions.getX(), 0.0F, (float)dimensions.getZ()).add(0.0F, 0.0F, 0.0F, 0.0F, (float)dimensions.getY(), 0.0F);
    }
}
