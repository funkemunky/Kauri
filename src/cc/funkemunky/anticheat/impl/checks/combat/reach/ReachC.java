package cc.funkemunky.anticheat.impl.checks.combat.reach;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
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
    public ReachC(String name, CancelType cancelType, int maxVL) {
        super(name, cancelType, maxVL);
    }

    @Setting
    private float boxExpand = 3.02f;

    @Setting
    private long range = 200;

    @Setting
    private double vlMax = 5;

    private double vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        WrappedInUseEntityPacket use = new WrappedInUseEntityPacket(packet, getData().getPlayer());

        if(use.getEntity() instanceof Player && use.getAction().equals(WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK) && use.getPlayer().getGameMode().equals(GameMode.SURVIVAL)) {
            val entityData = Kauri.getInstance().getDataManager().getPlayerData(use.getEntity().getUniqueId());

            if(entityData == null) return;
            List<CustomLocation> locations = entityData.getMovementProcessor().getPastLocation().getEstimatedLocation(getData().getTransPing(), range + Math.abs(getData().getLastTransPing() - getData().getTransPing()));

            List<BoundingBox> boxes = new ArrayList<>();

            BoundingBox playerBox = new BoundingBox(getData().getMovementProcessor().getTo().clone().toLocation(use.getPlayer().getWorld())
                    .add(0, 1.53, 0).toVector(), getData().getMovementProcessor().getTo().clone().toLocation(use.getPlayer().getWorld()).add(0, 1.53, 0).toVector())
                    .grow(boxExpand, boxExpand, boxExpand);

            locations.forEach(loc -> boxes.add(getHitbox(loc)));

            if(boxes.stream().noneMatch(box -> box.collides(playerBox))) {
                if(vl++ > vlMax) {
                    flag("reach is greater than 3.1", false, true);
                }
                debug("VL: "+ vl + "REACH: " + boxExpand + " RANGE: " + 200);
            } else {
                vl -= vl > 0 ? 0.1f : 0;
            }
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }

    private BoundingBox getHitbox(CustomLocation l) {
        return new BoundingBox(0,0,0,0,0,0).add((float) l.getX(), (float) l.getY(), (float) l.getZ()).grow(.32f, 0, .32f)
                .add(0,0,0,0, 1.85f, 0);
    }
}