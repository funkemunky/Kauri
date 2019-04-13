package cc.funkemunky.anticheat.impl.checks.movement.fly;

import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MathUtils;
import lombok.val;
import org.bukkit.event.Event;

@CheckInfo(name = "Fly (Type E)", description = "Detects flys.", type = CheckType.FLY, executable = false)
@Init
@Packets(packets = {Packet.Client.LEGACY_POSITION, Packet.Client.LEGACY_POSITION_LOOK, Packet.Client.POSITION_LOOK, Packet.Client.POSITION})
public class FlyE extends Check {

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        if (MiscUtils.cancelForFlight(getData(), 15, false)) return;

        if (move.getAirTicks() > 5 && !move.isClientOnGround() && !MathUtils.approxEquals(0.2, move.getServerYVelocity(), move.getDeltaY())) {
            flag(move.getDeltaY() + ">-" + move.getServerYVelocity(), true, true);
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
