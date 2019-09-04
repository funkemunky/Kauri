package cc.funkemunky.anticheat.impl.checks.movement.fly;

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

@CheckInfo(name = "Fly (Type B)", description = "Checks for a player double jumping impossibly.", type = CheckType.FLY, maxVL = 60)
@Init
@Packets(packets = {Packet.Client.POSITION, Packet.Client.POSITION_LOOK})
public class FlyB extends Check {

    private Verbose vl = new Verbose();
    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        if(!move.isNearGround() && !move.isCancelFlight() && move.getDeltaY() > move.getLastDeltaY() + 0.01) {
            if(vl.flag(1, 2000L)) {
                flag(move.getDeltaXZ() + ">-" + move.getLastDeltaXZ(),
                        true,
                        true,
                        AlertTier.LIKELY);
            }
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
