package cc.funkemunky.anticheat.impl.checks.combat.hitboxes;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.*;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.utils.BoundingBox;
import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.math.RayTrace;
import lombok.val;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Packets(packets = {
        Packet.Client.POSITION,
        Packet.Client.POSITION_LOOK,
        Packet.Client.LOOK,
        Packet.Client.FLYING,
        Packet.Client.LEGACY_POSITION_LOOK,
        Packet.Client.LEGACY_POSITION,
        Packet.Client.LEGACY_LOOK})
public class HitBox extends Check {
    @Setting()
    private int pingLeniency = 200;

    @Setting()
    private int maxVL = 8;

    private int vl;
    private LivingEntity target;

    private List<EntityType> type = new ArrayList<>(Arrays.asList(EntityType.PLAYER, EntityType.VILLAGER, EntityType.SKELETON, EntityType.BLAZE, EntityType.ZOMBIE, EntityType.PIG_ZOMBIE, EntityType.CREEPER, EntityType.SNOWMAN));

    public HitBox(String name, String description, CheckType type, CancelType cancelType, int maxVL, boolean enabled, boolean executable, boolean cancellable) {
        super(name, description, type, cancelType, maxVL, enabled, executable, cancellable);
    }

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(getData().getLastAttack().hasNotPassed(0) && target != null && type.contains(target.getType()) && target.getWorld().getUID().equals(getData().getPlayer().getWorld().getUID())) {
            PastLocation location = getData().getEntityPastLocation();
            if (getData().getTransPing() > 400) return;
            List<BoundingBox> boxes = new ArrayList<>();

<<<<<<< HEAD
            val locs = location.getEstimatedLocation(getData().getTransPing(), Math.abs(getData().getTransPing() - getData().getLastTransPing()) + pingLeniency);
=======
            if(use.getEntity() instanceof LivingEntity) {
                target = (LivingEntity) use.getEntity();
                lastAttack.reset();
            }
        } else if(target != null && target.getWorld().getUID().equals(getData().getPlayer().getWorld().getUID()) && type.contains(target.getType())) {
            PastLocation location;
            if(target instanceof Player) {
                val entityData = Kauri.getInstance().getDataManager().getPlayerData(target.getUniqueId());

                if(entityData != null) {
                    location = entityData.getMovementProcessor().getPastLocation();
                } else {
                    mobLocation.addLocation(target.getLocation());
                    location = mobLocation;
                }
            } else {
                mobLocation.addLocation(target.getLocation());
                location = mobLocation;
            }

            if(lastAttack.hasNotPassed()) {
                if (getData().getTransPing() > 400) return;
                List<BoundingBox> boxes = new ArrayList<>();
>>>>>>> aff83c727800c6466137f8e7263de1882b69d326

            if (locs.size() == 0) return;
            locs.forEach(loc -> boxes.add(getHitbox(target, loc)));
            val eyeLoc = getData().getMovementProcessor().getTo().clone();

            eyeLoc.setY(eyeLoc.getY() + (getData().getPlayer().isSneaking() ? 1.53 : getData().getPlayer().getEyeHeight()));

            RayTrace trace = new RayTrace(eyeLoc.toVector(), getData().getPlayer().getEyeLocation().getDirection());

            int collided = (int) boxes.stream()
                    .filter(box -> trace.intersects(box, box.getMinimum().distance(eyeLoc.toVector()) + 1.0, 0.2)).count();

<<<<<<< HEAD
            if (collided == 0 && !getData().isLagging()) {
                if (vl++ > maxVL) {
                    flag(collided + "=0", true, false);
=======
                int collided = (int) boxes.stream()
                        .filter(box -> trace.intersects(box, box.getMinimum().distance(eyeLoc.toVector()) + 1.0, 0.2)).count();

                if (collided == 0 && !getData().isLagging()) {
                    if (vl++ > maxVL) {
                        flag(collided + "=0", true, true);
                    }
                } else {
                    vl -= vl > 0 ? 2 : 0;
>>>>>>> aff83c727800c6466137f8e7263de1882b69d326
                }
            } else {
                vl -= vl > 0 ? 2 : 0;
            }

            debug("VL: " + vl + " COLLIDED: " + collided + " LOCSIZE: " + locs.size() + " PING: " + getData().getTransPing() + " BOXSIZE: " + boxes.size() + " DELTA: " + Math.abs(getData().getTransPing() - getData().getLastTransPing()) + pingLeniency);
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }

    private BoundingBox getHitbox(LivingEntity entity, CustomLocation l) {
        Vector dimensions = MiscUtils.entityDimensions.getOrDefault(entity.getType(), new Vector(0.4, 2,0.4));
        return new BoundingBox(0, 0, 0, 0, 0, 0).add((float) l.getX(), (float) l.getY(), (float) l.getZ()).grow((float) dimensions.getX(), (float) dimensions.getY(), (float) dimensions.getZ()).grow(.1f, 0.1f, .1f)
                .grow((entity.getVelocity().getY() > 0 ? 0.15f : 0) + getData().getMovementProcessor().getDeltaXZ() / 1.25f, 0, (entity.getVelocity().getY() > 0 ? 0.15f : 0) + getData().getMovementProcessor().getDeltaXZ() / 1.25f);
    }
}
