package cc.funkemunky.anticheat.impl.checks.movement.fly;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import lombok.val;
import org.bukkit.event.Event;

@CheckInfo(name = "Fly (Type B)", description = "Checks for a player double jumping impossibly.", type = CheckType.FLY)
@Init
@Packets(packets = {Packet.Client.POSITION, Packet.Client.POSITION_LOOK})
public class FlyB extends Check {

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        if(!move.isNearGround() && !MiscUtils.cancelForFlight(getData(), 10, false) && move.getDeltaXZ() > move.getLastDeltaXZ() + 0.01) {
            flag(move.getDeltaXZ() + ">-" + move.getLastDeltaXZ(), true, true, AlertTier.HIGH);
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
