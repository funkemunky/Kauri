package cc.funkemunky.anticheat.impl.checks.movement.groundspoof;

import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Verbose;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.POSITION, Packet.Client.LEGACY_POSITION, Packet.Client.LEGACY_POSITION_LOOK})
@CheckInfo(name = "GroundSpoof (Type C)", description = "Looks for invalid onGround packets while not in the air.")
@Init
public class GroundSpoofC extends Check {

    private Verbose verbose = new Verbose();

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        if (getData().isGeneralCancel() || getData().getLastServerPos().hasNotPassed(1)) return;

        if (move.isServerOnGround() && !move.isClientOnGround() && move.getGroundTicks() > 4) {
            if (verbose.flag(4, 350L)) {
                flag("t: " + move.getGroundTicks() + " v: " + verbose.getVerbose(), true, true);
            }
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
