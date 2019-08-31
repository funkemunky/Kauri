package cc.funkemunky.anticheat.impl.checks.movement.groundspoof;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Verbose;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import lombok.val;
import org.bukkit.event.Event;

@Init
@CheckInfo(name = "GroundSpoof (Type C)", description = "Checks for onGround spoofing in air, which is impossible.", type = CheckType.MOVEMENT, maxVL = 10)
@Packets(packets = {Packet.Client.POSITION, Packet.Client.POSITION_LOOK})
public class GroundSpoofC extends Check {

    private Verbose verbose = new Verbose();

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        if(move.isClientOnGround() && !move.isNearGround() && !move.isServerPos()) {
            if(verbose.flag(1, 2000L)) {
                flag("airTicks=" + move.getAirTicks() + " fallDistance=" + move.getFallDistance(), true, true, AlertTier.HIGH);
            }
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
