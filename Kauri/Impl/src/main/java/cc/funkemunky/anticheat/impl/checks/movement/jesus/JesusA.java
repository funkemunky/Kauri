package cc.funkemunky.anticheat.impl.checks.movement.jesus;

import cc.funkemunky.anticheat.api.checks.*;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Verbose;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {Packet.Client.POSITION, Packet.Client.POSITION_LOOK})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "Jesus (Type A)", description = "Looks for consistency in y movement in water.", type = CheckType.JESUS, maxVersion = ProtocolVersion.V1_12_2, cancelType = CancelType.MOTION, maxVL = 50)
public class JesusA extends Check {

    private Verbose verbose = new Verbose();

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        if (move.isNearLiquid() && !move.isNearGround() && !move.isBlocksOnTop() && !getData().isGeneralCancel()) {
            if (Math.abs(move.getDeltaY()) < 1E-4) {
                if (verbose.flag(12, 500L)) {
                    flag(move.getDeltaY() + "b/s", true, true, AlertTier.LIKELY);
                }
            } else verbose.deduct(2);
            debug(move.getDeltaY() + "");
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
