package cc.funkemunky.anticheat.impl.checks.combat.reach;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.CustomLocation;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.utils.BoundingBox;
import lombok.val;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.List;

@Packets(packets = {Packet.Client.USE_ENTITY})
public class ReachC extends Check {
    public ReachC(String name, String description, CheckType type, CancelType cancelType, int maxVL, boolean enabled, boolean executable, boolean cancellable) {
        super(name, description, type, cancelType, maxVL, enabled, executable, cancellable);
    }

    @Setting(name = "boxExpand")
    private float boxExpand = 3.0f;

    @Setting(name = "range")
    private long range = 200;

    @Setting(name = "vlMax")
    private double vlMax = 5;

    private double vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        WrappedInUseEntityPacket use = new WrappedInUseEntityPacket(packet, getData().getPlayer());

        if (getData().isGeneralCancel()) return;
        if (use.getEntity() instanceof Player && use.getAction().equals(WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK) && use.getPlayer().getGameMode().equals(GameMode.SURVIVAL)) {
            val entityData = Kauri.getInstance().getDataManager().getPlayerData(use.getEntity().getUniqueId());

            if (entityData == null) return;
            List<CustomLocation> locations = entityData.getMovementProcessor().getPastLocation().getEstimatedLocation(getData().getTransPing(), range + Math.abs(getData().getLastTransPing() - getData().getTransPing()));

            List<BoundingBox> boxes = new ArrayList<>();

            BoundingBox playerBox = new BoundingBox(getData().getMovementProcessor().getTo().clone().toLocation(use.getPlayer().getWorld())
                    .add(0, 1.53, 0).toVector(), getData().getMovementProcessor().getTo().clone().toLocation(use.getPlayer().getWorld()).add(0, 1.53, 0).toVector())
                    .grow(boxExpand, boxExpand, boxExpand);

            locations.forEach(loc -> boxes.add(getHitbox(loc)));

            val count = boxes.stream().filter(box -> box.collides(playerBox)).count();
            if (count == 0 && !getData().isLagging()) {
                if (vl++ > vlMax) {
                    flag("reach is greater than " + boxExpand, false, true);
                }
                debug("VL: " + vl + "REACH: " + boxExpand + " RANGE: " + 200);
            } else {
                vl -= vl > 0 ? 0.5f : 0;
            }

            debug("COUNT: " + count + " VL: " + vl + "/" + vlMax);
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }

    private BoundingBox getHitbox(CustomLocation l) {
        return new BoundingBox(0, 0, 0, 0, 0, 0).add((float) l.getX(), (float) l.getY(), (float) l.getZ()).grow(.4f, 0, .4f)
                .add(0, 0, 0, 0, 1.85f, 0);
    }
}