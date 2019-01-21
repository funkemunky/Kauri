package cc.funkemunky.anticheat.impl.checks.combat.hitboxes;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.utils.CustomLocation;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.utils.BoundingBox;
import cc.funkemunky.api.utils.math.RayTrace;
import lombok.val;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.List;

@Packets(packets = {Packet.Client.USE_ENTITY})
public class HitBox extends Check {
    @Setting
    private int pingLeniency = 200;

    @Setting
    private int maxVL = 8;


    private int vl;

    public HitBox(String name, CancelType cancelType, int maxVL) {
        super(name, cancelType, maxVL);
    }

    @Override
    public Object onPacket(Object packet, String packetType, long timeStamp) {
        WrappedInUseEntityPacket use = new WrappedInUseEntityPacket(packet, getData().getPlayer());

        if (use.getEntity() instanceof Player) {
            val entityData = Kauri.getInstance().getDataManager().getPlayerData(use.getEntity().getUniqueId());

            if (entityData == null || entityData.getTransPing() > 400 || getData().getTransPing() > 400) return packet;
            List<BoundingBox> boxes = new ArrayList<>();

            val locs = entityData.getMovementProcessor().getPastLocation().getEstimatedLocation(getData().getTransPing(), Math.abs(getData().getTransPing() - getData().getLastTransPing()) + pingLeniency);

            if (locs.size() == 0) return packet;
            locs.forEach(loc -> boxes.add(getHitbox(loc)));
            val eyeLoc = getData().getMovementProcessor().getTo().clone();

            eyeLoc.setY(eyeLoc.getY() + (getData().getPlayer().isSneaking() ? 1.53 : getData().getPlayer().getEyeHeight()));

            RayTrace trace = new RayTrace(eyeLoc.toVector(), getData().getPlayer().getEyeLocation().getDirection());

            int collided = (int) boxes.stream()
                    .filter(box -> trace.intersects(box, box.getMinimum().distance(eyeLoc.toVector()) + 1.0, 0.2)).count();

            if (collided == 0) {
                if (vl++ > maxVL) {
                    flag(collided + "=0", true, false);
                }
            } else {
                vl -= vl > 0 ? 1 : 0;
            }

            debug("VL: " + vl + " COLLIDED: " + collided + " LOCSIZE: " + locs.size() + " PING: " + getData().getTransPing() + " BOXSIZE: " + boxes.size() + " DELTA: " + Math.abs(getData().getTransPing() - getData().getLastTransPing()) + pingLeniency);
        }
        return packet;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }

    private BoundingBox getHitbox(CustomLocation l) {
        return new BoundingBox(0, 0, 0, 0, 0, 0).add((float) l.getX(), (float) l.getY(), (float) l.getZ()).grow(.4f, 0, .4f)
                .add(0, 0, 0, 0, 1.85f, 0);
    }
}
