package cc.funkemunky.anticheat.impl.checks.combat.hitboxes;

import cc.funkemunky.anticheat.api.checks.*;
import cc.funkemunky.anticheat.api.utils.CustomLocation;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.PastLocation;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.BoundingBox;
import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.math.RayTrace;
import lombok.val;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
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
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "HitBox", description = "A very accurate hit-box check, using a mixture of ray-tracing and bounding-boxes.", type = CheckType.COMBAT, cancelType = CancelType.COMBAT, maxVL = 20)
public class HitBox extends Check {
    @Setting(name = "pingLeniency")
    private int pingLeniency = 200;

    @Setting(name = "threshold.vl.max")
    private int maxVL = 14;

    private int vl;

    private List<EntityType> type = new ArrayList<>(Arrays.asList(EntityType.PLAYER, EntityType.VILLAGER, EntityType.SKELETON, EntityType.BLAZE, EntityType.ZOMBIE, EntityType.PIG_ZOMBIE, EntityType.CREEPER, EntityType.SNOWMAN));

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val target = getData().getTarget();
        if (getData().getLastAttack().hasNotPassed(0) && target != null && type.contains(target.getType()) && target.getWorld().getUID().equals(getData().getPlayer().getWorld().getUID())) {
            PastLocation location = getData().getEntityPastLocation();
            if (getData().getTransPing() > 400 || getData().getMovementProcessor().getYawDelta() > 9) return;
            List<BoundingBox> boxes = new ArrayList<>();

            val locs = location.getEstimatedLocation(getData().getTransPing(), Math.abs(getData().getTransPing() - getData().getLastTransPing()) + pingLeniency);

            if (locs.size() == 0) return;
            locs.forEach(loc -> boxes.add(getHitbox(target, loc)));
            val eyeLoc = getData().getMovementProcessor().getTo().clone();

            eyeLoc.setY(eyeLoc.getY() + (getData().getPlayer().isSneaking() ? 1.53 : getData().getPlayer().getEyeHeight()));

            RayTrace trace = new RayTrace(eyeLoc.toVector(), getData().getPlayer().getEyeLocation().getDirection());

            int collided = (int) boxes.stream()
                    .filter(box -> trace.intersects(box, box.getMinimum().distance(eyeLoc.toVector()) + 1.0, 0.2)).count();
            if (collided == 0 && !getData().isLagging()) {
                if(vl++ > maxVL * 1.5) {
                    flag(collided + "=" + 0, true, true, !getData().isLagging() ? AlertTier.CERTAIN : AlertTier.HIGH);
                } else if (vl > maxVL) {
                    flag(collided + "=0", true, true, !getData().isLagging() ? AlertTier.HIGH : AlertTier.LIKELY);
                }
            } else {
                vl -= vl > 0 ? 1 : 0;
            }

            debug("VL: " + vl + " COLLIDED: " + collided + " LOCSIZE: " + locs.size() + " PING: " + getData().getTransPing() + " BOXSIZE: " + boxes.size() + " DELTA: " + Math.abs(getData().getTransPing() - getData().getLastTransPing()) + pingLeniency);
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }

    private BoundingBox getHitbox(LivingEntity entity, CustomLocation l) {
        Vector dimensions = MiscUtils.entityDimensions.getOrDefault(entity.getType(), new Vector(0.4, 2, 0.4));
        return new BoundingBox(0, 0, 0, 0, 0, 0).add((float) l.getX(), (float) l.getY(), (float) l.getZ()).grow((float) dimensions.getX(), (float) dimensions.getY(), (float) dimensions.getZ()).grow(.25f, 0.15f, .25f);
    }
}
