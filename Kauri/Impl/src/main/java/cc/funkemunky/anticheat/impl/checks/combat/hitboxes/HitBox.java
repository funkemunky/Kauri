package cc.funkemunky.anticheat.impl.checks.combat.hitboxes;

import cc.funkemunky.anticheat.api.checks.*;
import cc.funkemunky.anticheat.api.utils.CustomLocation;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.RayTrace;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.BoundingBox;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.MiscUtils;
import lombok.val;
import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;

@CheckInfo(name = "HitBox", description = "Ensures that the player is not using any expanded form of a player hitbox.", type = CheckType.REACH, cancelType = CancelType.COMBAT)
@Packets(packets = {Packet.Client.FLYING, Packet.Client.POSITION_LOOK, Packet.Client.LOOK, Packet.Client.POSITION, Packet.Client.ARM_ANIMATION})
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

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(packetType.equalsIgnoreCase(Packet.Client.ARM_ANIMATION)) {
            vl-= vl > 0 ? 0.05 : 0;
        } else if(getData().getTarget() != null && allowedEntities.contains(getData().getTarget().getType()) && getData().getLastAttack().hasNotPassed(0) && !getData().getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
            val move = getData().getMovementProcessor();

            val origin = move.getTo().toLocation(getData().getPlayer().getWorld()).add(0, 1.53f, 0);

            val pastLoc = move.getPastLocation().getEstimatedLocation(getData().getTransPing(), 200);

            val hitbox = getData().getEntityPastLocation().getEstimatedLocation(0, 150);

            val doesMatch = pastLoc.stream().map(loc -> new RayTrace(loc.toVector().add(new Vector(0, 1.53f, 0)), loc.toLocation(getData().getPlayer().getWorld()).add(0, 1.53, 0).getDirection()).traverse(3.4, 0.1, 0.05, Math.min(2, getData().getTarget().getLocation().distance(getData().getPlayer().getLocation()) / 2))).anyMatch(vecList -> vecList.parallelStream().anyMatch(vec -> hitbox.stream().anyMatch(vec2 -> getHitbox(getData().getTarget(), vec2).collides(vec))));

            if(!doesMatch && !getData().isLagging()) {
                val reach = hitbox.stream().mapToDouble(loc -> loc.toVector().add(new Vector(0, 1.53, 0)).distance(origin.toVector())).average().orElse(0);

                if(vl++ > 8) {
                    flag("distance=" + reach + " vl=" + vl, true, true, AlertTier.HIGH);
                }
                debug("vl=" + vl + " distance=" + reach);
            } else vl-= vl > 0 ? 1 : 0;
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }

    private BoundingBox getHitbox(LivingEntity entity, CustomLocation l) {
        Vector dimensions = MiscUtils.entityDimensions.getOrDefault(entity.getType(), new Vector(0.35F, 1.85F, 0.35F));
        return (new BoundingBox(l.toVector(), l.toVector())).grow(0.24F, 0.2F, 0.24F).grow((float)dimensions.getX(), 0.0F, (float)dimensions.getZ()).add(0.0F, 0.0F, 0.0F, 0.0F, (float)dimensions.getY(), 0.0F);
    }
}
