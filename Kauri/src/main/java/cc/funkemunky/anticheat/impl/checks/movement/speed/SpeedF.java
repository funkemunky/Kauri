package cc.funkemunky.anticheat.impl.checks.movement.speed;

import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MathUtils;
import lombok.val;
import org.bukkit.event.Event;

@CheckInfo(name = "Speed (Type F)", description = "Speed check", type = CheckType.SPEED, maxVL = 10)
//@Init
@Packets(packets = {Packet.Client.POSITION, Packet.Client.POSITION_LOOK, Packet.Client.LEGACY_POSITION_LOOK, Packet.Client.LEGACY_POSITION})
public class SpeedF extends Check {

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        if(getData().isGeneralCancel() && getData().getLastLogin().hasPassed(20)) return;

        val delta = MathUtils.getDelta(move.getDeltaXZ(), move.getLastDeltaXZ());

        if(delta > 0.4 * (getData().getMovementProcessor().getAirTicks() > 0 && getData().getMovementProcessor().getAirTicks() < 2 ? 2.5 : 1)) {
            flag("delta=" + delta, true, true);
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
