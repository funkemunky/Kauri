package cc.funkemunky.anticheat.impl.checks.player.badpackets;

import cc.funkemunky.anticheat.api.checks.*;
import cc.funkemunky.anticheat.api.utils.BukkitEvents;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.RayTrace;
import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockDigPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockPlacePacket;
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
@Packets(packets = {Packet.Client.BLOCK_PLACE})
@CheckInfo(name = "BadPackets (Type J)", type = CheckType.BADPACKETS, cancelType = CancelType.PLACE)
public class BadPacketsJ extends Check {
    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        WrappedInBlockPlacePacket place = new WrappedInBlockPlacePacket(packet, getData().getPlayer());

    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
