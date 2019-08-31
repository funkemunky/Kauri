package cc.funkemunky.anticheat.impl.checks.combat.hitboxes;

import cc.funkemunky.anticheat.api.checks.*;
import cc.funkemunky.anticheat.api.utils.CustomLocation;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.RayTrace;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.BoundingBox;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MiscUtils;
import lombok.val;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@CheckInfo(name = "HitBox", description = "Ensures that the player is not using any expanded form of a player hitbox.", type = CheckType.REACH, cancelType = CancelType.COMBAT)
@Packets(packets = {Packet.Client.FLYING, Packet.Client.POSITION_LOOK, Packet.Client.LOOK, Packet.Client.POSITION})
@Init
public class HitBox extends Check {

    private double vl;
    private List<EntityType> allowedEntities = Arrays.asList(
            EntityType.ZOMBIE,
            EntityType.VILLAGER,
            EntityType.PLAYER,
            EntityType.SKELETON,
            EntityType.WITCH,
            EntityType.CREEPER,
            EntityType.ENDERMAN);
    private long lastTimeStamp;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(timeStamp - lastTimeStamp <= 4) {
            lastTimeStamp = timeStamp;
            return;
        }
        lastTimeStamp = timeStamp;
        if(getData().getLastAttack().hasNotPassed(0) && getData().getTarget() != null && allowedEntities.contains(getData().getTarget().getType())) {
            val move = getData().getMovementProcessor();

            val locs = move.getPastLocation().getEstimatedLocation(0, (move.getYawDelta() > 3 ? 150 : 100) + (getData().getTransPing() - getData().getLastTransPing()) * 2).stream().map(loc -> loc.add(0, getData().getPlayer().getEyeHeight(), 0L)).collect(Collectors.toList());

            List<BoundingBox> hitbox = getData().getEntityPastLocation().getEstimatedLocation(getData().getTransPing(), (move.getYawDelta() > 5 ? 200 : 150) + (getData().getTransPing() - getData().getLastTransPing()) * 2).stream().map(loc -> getHitbox(getData().getTarget(), loc)).collect(Collectors.toList());
            val collided = locs.stream().filter(loc -> new RayTrace(loc.toVector(), loc.toLocation(getData().getPlayer().getWorld()).getDirection()).traverse(3.1f, 0.1, 0.025, 2.5).parallelStream().anyMatch(vec -> hitbox.stream().anyMatch(box -> box.collides(vec)))).collect(Collectors.toList());

            if(getData().getLastTargetSwitch().hasPassed() && collided.size() == 0 && !getData().isLagging()) {
                if(vl++ > 8) {
                    flag("vl=" + vl + " ping=" + getData().getTransPing(), true, true, vl > 12 ? AlertTier.HIGH : AlertTier.LIKELY);
                }
                debug(Color.Green + "Flag: " + vl);
            } else vl-= vl > 0 ? 0.2 : 0;
            debug("collided=" + collided.size() + " vl=" + vl + " ping=" + getData().getTransPing());
            collided.clear();
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }

    private BoundingBox getHitbox(LivingEntity entity, CustomLocation l) {
        Vector dimensions = MiscUtils.entityDimensions.getOrDefault(entity.getType(), new Vector(0.35F, 1.85F, 0.35F));
        return (new BoundingBox(l.toVector(), l.toVector())).grow(0.35F, 0.25F, 0.35F).grow((float)dimensions.getX(), 0.0F, (float)dimensions.getZ()).add(0.0F, 0.0F, 0.0F, 0.0F, (float)dimensions.getY(), 0.0F);
    }
}
