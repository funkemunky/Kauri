package cc.funkemunky.anticheat.impl.checks.combat.hitboxes;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.checks.*;
import cc.funkemunky.anticheat.api.utils.CustomLocation;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.RayTrace;
import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedPacketPlayOutWorldParticle;
import cc.funkemunky.api.tinyprotocol.packet.types.WrappedEnumParticle;
import cc.funkemunky.api.utils.BoundingBox;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MiscUtils;
import lombok.val;
import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@CheckInfo(name = "HitBox", description = "Ensures that the player is not using any expanded form of a player hitbox.",
        type = CheckType.REACH, cancelType = CancelType.COMBAT)
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
        val move = getData().getMovementProcessor();
        if(timeStamp - lastTimeStamp <= 4) {
            lastTimeStamp = timeStamp;
            return;
        }
        lastTimeStamp = timeStamp;

        if(getData().getLastAttack().hasNotPassed(0)
                && getData().getTarget() != null
                && allowedEntities.contains(getData().getTarget().getType())
                && !getData().isCreativeMode()) {

            val rayTrace = move.getPastLocation()
                    .getEstimatedLocation(0, 100L)
                    .stream()
                    .map(loc ->
                            loc.toLocation(getData().getPlayer().getWorld()).clone()
                            .add(0, getData().getPlayer().getEyeHeight(), 0))
                    .map(loc -> new RayTrace(loc.toVector(), loc.getDirection()))
                    .collect(Collectors.toList());

            List<Vector> vectors = new ArrayList<>();
            rayTrace.stream().map(trace -> trace.traverse(3.2f, 0.1f)).forEach(vectors::addAll);

            val entityLocations = getData().getEntityPastLocation()
                    .getEstimatedLocation(getData().getTransPing(), 150L)
                    .stream()
                    .map(loc -> getHitbox(getData().getTarget(), loc))
                    .collect(Collectors.toList());

            List<Vector> collided = new ArrayList<>();
            for (BoundingBox box : entityLocations) {
                vectors.parallelStream().filter(box::collides).forEach(collided::add);
            }

            if(collided.size() == 0) {
                if(vl++ > 6) {
                    flag("collided=0 ping=" + getData().getTransPing() + " vl=" + vl,
                            true,
                            true,
                            AlertTier.HIGH);
                }
            } else vl-= vl > 0 ? 0.25 : 0;

            debug("collided=" + collided.size());
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }

    private BoundingBox getHitbox(LivingEntity entity, CustomLocation l) {
        Vector dimensions = MiscUtils.entityDimensions
                .getOrDefault(entity.getType(), new Vector(0.35F, 1.85F, 0.35F));
        return (new BoundingBox(l.toVector(), l.toVector()))
                .grow(0.2f, 0.15F, 0.2f)
                .grow((float)dimensions.getX(), 0.0F, (float)dimensions.getZ())
                .add(0.0F, 0.0F, 0.0F, 0.0F, (float)dimensions.getY(), 0.0F);
    }
}