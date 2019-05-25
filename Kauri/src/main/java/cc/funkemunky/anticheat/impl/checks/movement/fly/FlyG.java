package cc.funkemunky.anticheat.impl.checks.movement.fly;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import lombok.val;
import org.bukkit.event.Event;

@CheckInfo(name = "Fly (Type G)", description = "Checks if the y is literally 0. 10/10", type = CheckType.FLY, maxVL = 20)
@Packets(packets = {Packet.Client.POSITION, Packet.Client.POSITION_LOOK})
public class FlyG extends Check {

    private int vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        if(move.getDeltaY() == 0 && !move.isServerOnGround()) {
            if(vl++ > 2 || move.getAirTicks() > 2) {
                flag("y=0", true, true, vl > 7 ? AlertTier.CERTAIN : AlertTier.HIGH);
            }
        } else vl-= vl > 0 ? 1 : 0;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
