package cc.funkemunky.anticheat.impl.checks.movement.groundspoof;

import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.POSITION, Packet.Client.LEGACY_POSITION, Packet.Client.LEGACY_POSITION_LOOK})
@CheckInfo(name = "GroundSpoof (Type C)", description = "Looks for invalid onGround packets while not in the air.")
public class GroundSpoofC extends Check {

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        if(getData().isGeneralCancel()) return;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
