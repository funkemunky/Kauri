package cc.funkemunky.anticheat.impl.checks.player.badpackets;

import cc.funkemunky.anticheat.api.checks.*;
import cc.funkemunky.anticheat.api.utils.BukkitEvents;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.RayTrace;
import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockPlacePacket;
import cc.funkemunky.api.utils.BlockUtils;
import cc.funkemunky.api.utils.BoundingBox;
import cc.funkemunky.api.utils.Init;
import lombok.val;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Init
@CheckInfo(name = "BadPackets (Type B)", type = CheckType.BADPACKETS, cancelType = CancelType.INTERACT)
@BukkitEvents(events = {BlockPlaceEvent.class})
public class BadPacketsB extends Check {

    private double vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
    }

    @Override
    public void onBukkitEvent(Event event) {
        BlockPlaceEvent e = (BlockPlaceEvent) event;

        if(e.getBlockPlaced() == null || !e.getBlockPlaced().getType().isSolid() || !e.getPlayer().getGameMode().equals(GameMode.SURVIVAL)) return;

        val move = getData().getMovementProcessor();
        val originLocs = move.getPastLocation().getEstimatedLocation(getData().getTransPing(), 100).stream().map(loc -> loc.clone().toLocation(getData().getPlayer().getWorld()).add(0, getData().getActionProcessor().isSneaking() ? 1.53f : 1.62f, 0)).collect(Collectors.toList());
        RayTrace trace;
        List<Vector> vecs = new ArrayList<>();

        for (Location origin : originLocs) {
            trace = new RayTrace(origin.toVector(), origin.getDirection());
            vecs.addAll(trace.traverse(4.6, 0.2));
        }
        List<BoundingBox> boxes = Atlas.getInstance().getBlockBoxManager().getBlockBox().getSpecificBox(e.getBlockPlaced().getLocation()).stream().map(box -> box.grow(0.15f, 0.15f, 0.15f)).collect(Collectors.toList());

        long count = vecs.stream().filter(vec -> boxes.stream().anyMatch(box -> box.collides(vec))).count();
        if(count == 0) {
            vl++;
            flag("none", true, true, vl > 1 ? AlertTier.HIGH : AlertTier.LIKELY);
        } else vl-= vl > 0 ? 0.5 : 0;

        debug("count=" + count + " vl=" + vl);
    }
}
