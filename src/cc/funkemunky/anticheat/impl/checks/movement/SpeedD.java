package cc.funkemunky.anticheat.impl.checks.movement;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.BlockUtils;
import cc.funkemunky.api.utils.ReflectionsUtil;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.Event;

import java.util.Vector;

@Packets(packets = {Packet.Client.LEGACY_POSITION, Packet.Client.LEGACY_POSITION_LOOK, Packet.Client.POSITION, Packet.Client.POSITION_LOOK})
public class SpeedD extends Check {
    public SpeedD(String name, CheckType type, CancelType cancelType, int maxVL) {
        super(name, type, cancelType, maxVL);
    }

    float lastMotion;

    @Override
    public Object onPacket(Object packet, String packetType, long timeStamp) {
        if (!getData().getPlayer().getAllowFlight()
                && getData().getPlayer().getVehicle() == null) {
            Block below = BlockUtils.getBlock(getData().getMovementProcessor().getTo().clone().toLocation(getData().getPlayer().getWorld()).subtract(0, 1, 0));

            val to = getData().getMovementProcessor().getTo();
            val from = getData().getMovementProcessor().getFrom();

            val deltaXZ = (float) Math.hypot(to.getX() - from.getX(), to.getZ() - from.getZ());
            val friction = !getData().getMovementProcessor().isServerOnGround()|| !below.getType().isSolid() ? 0.68f : ReflectionsUtil.getFriction(below);
            val resistance = getData().getMovementProcessor().isServerOnGround() ? 0.91f : friction * 0.91f;
            val predicted = lastMotion * resistance;
            val delta = deltaXZ - predicted;

            if(getData().getMovementProcessor().getAirTicks() > 2) {
                if (delta  > 0.2f) flag(delta + "", true, true);
                debug("DIFFERENCE: " + delta);
            }

           // Bukkit.broadcastMessage("DIFFERENCE: " + delta);
            lastMotion = deltaXZ;
        }
        return packet;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
