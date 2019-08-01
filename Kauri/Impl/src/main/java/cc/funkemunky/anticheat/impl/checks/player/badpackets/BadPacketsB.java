package cc.funkemunky.anticheat.impl.checks.player.badpackets;

import cc.funkemunky.anticheat.api.checks.*;
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
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Init
@CheckInfo(name = "BadPackets (Type B)", type = CheckType.BADPACKETS, cancelType = CancelType.INTERACT)
@Packets(packets = {Packet.Client.BLOCK_PLACE})
public class BadPacketsB extends Check {

    private int vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        WrappedInBlockPlacePacket place = new WrappedInBlockPlacePacket(packet, getData().getPlayer());

        if(place.getItemStack() != null && place.getItemStack().getType().isSolid()) {
            Block block = BlockUtils.getBlock(new Location(getData().getPlayer().getWorld(), place.getPosition().getX(), place.getPosition().getY(), place.getPosition().getZ()));

            if(block != null) {
                val move = getData().getMovementProcessor();
                val originLocs = move.getPastLocation().getEstimatedLocation(getData().getTransPing(), 100).stream().map(loc -> loc.clone().toLocation(getData().getPlayer().getWorld()).add(0, getData().getActionProcessor().isSneaking() ? 1.53f : 1.62f, 0)).collect(Collectors.toList());
                RayTrace trace;
                List<Vector> vecs = new ArrayList<>();

                for (Location origin : originLocs) {
                    trace = new RayTrace(origin.toVector(), origin.getDirection());
                    vecs.addAll(trace.traverse(getData().getPlayer().getGameMode().equals(GameMode.CREATIVE) ? 7 : 4.5, 0.2));
                }
                List<BoundingBox> boxes = Atlas.getInstance().getBlockBoxManager().getBlockBox().getSpecificBox(block.getLocation()).stream().map(box -> box.grow(0.25f, 0.25f, 0.25f)).collect(Collectors.toList());

                if(vecs.stream().noneMatch(vec -> boxes.stream().anyMatch(box -> box.collides(vec)))) {
                    if(vl++ > 2) {
                        flag("none", true, true, AlertTier.HIGH);
                    }
                } else vl-= vl > 0 ? 1 : 0;
            }
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
