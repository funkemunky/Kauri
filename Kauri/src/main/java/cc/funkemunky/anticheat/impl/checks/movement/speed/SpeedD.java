package cc.funkemunky.anticheat.impl.checks.movement.speed;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MathUtils;
import lombok.val;
import org.bukkit.event.Event;

@Init
@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.POSITION})
@CheckInfo(name = "Speed (Type D)", description = "Checks for consistent horizontal movements.", type = CheckType.SPEED, maxVL = 60)
public class SpeedD extends Check {

    private int vl;
    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();
        val delta = MathUtils.getDelta(move.getDeltaXZ(), move.getLastDeltaXZ());

        if(!move.isServerOnGround() && delta == 0) {
            if(vl++ > 2) {
                flag("delta==0", true, true, AlertTier.HIGH);
            }
        } else vl-= vl > 0 ? 1 : 0;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
