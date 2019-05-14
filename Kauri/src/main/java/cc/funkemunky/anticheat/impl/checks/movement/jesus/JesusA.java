package cc.funkemunky.anticheat.impl.checks.movement.jesus;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Verbose;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {Packet.Client.POSITION, Packet.Client.POSITION_LOOK, Packet.Client.LEGACY_POSITION_LOOK, Packet.Client.LEGACY_POSITION})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "Jesus (Type A)", description = "Looks for consistency in y movement in water.", type = CheckType.JESUS, maxVersion = ProtocolVersion.V1_12_2, cancelType = CancelType.MOTION, maxVL = 50)
public class JesusA extends Check {

    private Verbose verbose = new Verbose();

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        if (move.isInLiquid() && !move.isServerOnGround() && !getData().isGeneralCancel()) {
            if (Math.abs(move.getDeltaY()) < 1E-4) {
                if (verbose.flag(8, 500L)) {
                    flag(move.getDeltaY() + "b/s", true, true);
                }
            } else verbose.deduct();
            debug(move.getDeltaY() + "");
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
