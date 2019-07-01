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

//@Init
@CheckInfo(name = "Fly (Type E)", description = "Checks for air jump.", type = CheckType.FLY, maxVL = 50)
@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.POSITION})
public class FlyE extends Check {

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        if(!getData().isGeneralCancel() && move.isHasJumped() && move.getAirTicks() > 20 && getData().getLastBlockPlace().hasPassed(40)) {
            flag(move.getDeltaY() + ";" + move.isHasJumped() + ";" + move.getAirTicks(), true, true, AlertTier.LIKELY);
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
