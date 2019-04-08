package cc.funkemunky.anticheat.impl.checks.movement.fly;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.BlockUtils;
import cc.funkemunky.api.utils.MathUtils;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.POSITION, Packet.Client.LEGACY_POSITION, Packet.Client.LEGACY_POSITION_LOOK})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "Fly (Type B)", description = "A different style of acceleration check.", type = CheckType.FLY, cancelType = CancelType.MOTION)
public class FlyB extends Check {

    public FlyB() {

    }

    private double vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if (MiscUtils.cancelForFlight(getData(), 12, true)) return;

        val move = getData().getMovementProcessor();

        if(move.isBlocksAround()) return;

        if (!MathUtils.approxEquals(0.01, move.getLastClientYAcceleration(), move.getClientYAcceleration())) {
            if (vl++ > 4) {
                flag(move.getClientYAcceleration() + ", " + move.getLastClientYAcceleration(), true, true);
            }
        } else vl -= vl > 0 ? 0.75 : 0;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
