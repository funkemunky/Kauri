package cc.funkemunky.anticheat.impl.checks.player.badpackets;

import cc.funkemunky.anticheat.api.checks.*;
import cc.funkemunky.anticheat.api.utils.BukkitEvents;
import cc.funkemunky.anticheat.api.utils.RayTrace;
import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.utils.BlockUtils;
import cc.funkemunky.api.utils.BoundingBox;
import cc.funkemunky.api.utils.Init;
import lombok.val;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.stream.Collectors;

//@Init
@BukkitEvents(events = {BlockPlaceEvent.class})
@CheckInfo(name = "BadPackets (Type J)", type = CheckType.BADPACKETS, cancelType = CancelType.PLACE)
public class BadPacketsJ extends Check {
    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {

    }

    @Override
    public void onBukkitEvent(Event event) {
        BlockPlaceEvent e = (BlockPlaceEvent) event;

        if(e.getBlockPlaced() == null || !BlockUtils.isSolid(e.getBlockPlaced())) return;

        val originLocs = getData().getMovementProcessor().getPastLocation().getEstimatedLocation(getData().getTransPing(), 100).stream().map(loc -> loc.clone().toLocation(getData().getPlayer().getWorld()).add(0, getData().getActionProcessor().isSneaking() ? 1.53f : 1.62f, 0)).collect(Collectors.toList());


        int count = 0;
        for (Location origin : originLocs) {
            RayTrace trace = new RayTrace(origin.toVector(), origin.getDirection());

            List<BoundingBox> boxes = Atlas.getInstance().getBlockBoxManager().getBlockBox().getSpecificBox(e.getBlockPlaced().getLocation());
            for (Vector vec : trace.traverse(4.6, 0.2)) {
                Location loc = vec.toLocation(e.getBlockPlaced().getWorld());

                if(boxes.stream().anyMatch(box -> box.collides(vec))) break;

                List<BoundingBox> vecBoxes = Atlas.getInstance().getBlockBoxManager().getBlockBox().getSpecificBox(loc);

                if(loc.getBlock() != e.getBlockPlaced() && BlockUtils.isSolid(loc.getBlock()) && vecBoxes.stream().anyMatch(box -> box.collides(vec))) {
                    count++;
                    break;
                }
            }
        }

        if(count == 2) {
            flag("didnt look at block", true, true, AlertTier.HIGH);
        }
        debug("count=" + count);
    }
}
