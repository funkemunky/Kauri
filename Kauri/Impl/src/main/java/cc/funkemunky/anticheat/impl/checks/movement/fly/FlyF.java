package cc.funkemunky.anticheat.impl.checks.movement.fly;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import lombok.val;
import org.bukkit.event.Event;

@Init
@CheckInfo(name = "Fly (Type F)", description = "Checks for a bobbing movement far up into the air.", type = CheckType.FLY, maxVL = 60)
@Packets(packets = {Packet.Client.POSITION, Packet.Client.POSITION_LOOK})
public class FlyF extends Check {

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        if(move.getDeltaXZ() > move.getLastDeltaXZ() + 0.05 && move.getAirTicks() > 20 && !move.isNearGround() && getData().getLastBlockPlace().hasPassed(40) && !getData().isGeneralCancel()) {
            flag(move.getDeltaXZ() + ">-" + move.getLastDeltaXZ(), true, true, AlertTier.LIKELY);
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
