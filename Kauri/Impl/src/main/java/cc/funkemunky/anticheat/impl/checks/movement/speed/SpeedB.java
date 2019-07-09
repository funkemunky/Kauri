package cc.funkemunky.anticheat.impl.checks.movement.speed;

import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.POSITION})
public class SpeedB extends Check {

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        if(move.getAirTicks() > 2) {

        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
