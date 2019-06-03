package cc.funkemunky.anticheat.impl.checks.combat.reach;

import cc.funkemunky.anticheat.api.checks.*;
import cc.funkemunky.anticheat.api.utils.CustomLocation;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.BoundingBox;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.math.RayTrace;
import lombok.val;
import org.bukkit.GameMode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

@CheckInfo(name = "Reach", description = "A very accurate and fast 3.1 reach check.", type = CheckType.REACH, cancelType = CancelType.COMBAT, maxVL = 40)
@Packets(packets = {Packet.Client.ARM_ANIMATION, Packet.Client.LOOK, Packet.Client.POSITION, Packet.Client.POSITION_LOOK, Packet.Client.FLYING, Packet.Client.LEGACY_POSITION_LOOK, Packet.Client.LEGACY_POSITION, Packet.Client.LEGACY_LOOK})
@Init
public class Reach extends Check {

    private float vl = 0;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(getData().getPlayer().getGameMode().equals(GameMode.CREATIVE)) return;
        if(packetType.equals(Packet.Client.ARM_ANIMATION)) {
            vl-= vl > 0 ? 0.025 : 0;
        } else {
            val target = getData().getTarget();
            val move = getData().getMovementProcessor();

            if(target != null && getData().getLastLogin().hasPassed(5) && !getData().isServerPos() && getData().getLastAttack().hasNotPassed(0) && getData().getTransPing() < 450) {
                long range = (move.getYawDelta() > 4 ? 150 : 100) + Math.abs(getData().getTransPing() - getData().getLastTransPing()) * 3;
                val location = getData().getEntityPastLocation().getEstimatedLocation(getData().getTransPing(), range);
                val to = move.getTo().toLocation(getData().getPlayer().getWorld()).add(0, getData().getActionProcessor().isSneaking() ? 1.53f : 1.64f, 0);
                val trace = new RayTrace(to.toVector(), to.getDirection());

                val calcDistance = target.getLocation().distance(to);

                if(calcDistance > 15) {
                    if(vl++ > 4) {
                        flag(calcDistance + ">-20", true, true, AlertTier.HIGH);
                    }
                    return;
                } else if(calcDistance < 1) {
                    vl-= vl > 0 ? 0.1 : 0;
                    return;
                }

                val traverse = trace.traverse(calcDistance, 0.1);
                float distance = (float)traverse.stream()
                        .filter((vec) -> location.stream().anyMatch((loc) -> getHitbox(target, loc).intersectsWithBox(vec)))
                        .mapToDouble((vec) -> vec.distance(to.toVector()))
                        .min().orElse(0.0D);

                if(distance <= 2) {
                    return;
                }

                if(distance > 3.0F && getData().getMovementProcessor().getLagTicks() == 0) {
                    if(vl++ > 12) {
                        flag("reach=" + distance, true, true, AlertTier.CERTAIN);
                    } else if(vl > 7.0F) {
                        flag("reach=" + distance, true, true, AlertTier.HIGH);
                    } else if(vl > 3) {
                        flag("reach=" + distance, true, false, distance > 3.2 && vl > 5 ? AlertTier.LIKELY : AlertTier.POSSIBLE);
                    } else {
                        flag("reach=" + distance, true, false, AlertTier.LOW);
                    }
                } else vl-= vl > 0 ? 0.1 : 0;

                debug((distance > 3 ? Color.Green : "") + "distance=" + distance + " vl=" + vl + " range=" + range);
            }
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }

    private BoundingBox getHitbox(LivingEntity entity, CustomLocation l) {
        Vector dimensions = MiscUtils.entityDimensions.getOrDefault(entity.getType(), new Vector(0.35F, 1.85F, 0.35F));
        return (new BoundingBox(l.toVector(), l.toVector())).grow(0.15F, 0.15F, 0.15F).grow((float)dimensions.getX(), 0.0F, (float)dimensions.getZ()).add(0.0F, 0.0F, 0.0F, 0.0F, (float)dimensions.getY(), 0.0F);
    }
}
