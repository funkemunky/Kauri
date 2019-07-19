package cc.funkemunky.anticheat.impl.checks.movement.fly;

import cc.funkemunky.anticheat.api.checks.AlertTier;
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

@Init
@Packets(packets = {Packet.Client.POSITION, Packet.Client.POSITION_LOOK})
@CheckInfo(name = "Fly (Type A)", description = "Ensures the acceleration of a player is legitimate.", type = CheckType.FLY, maxVL = 50)
public class FlyA extends Check {

    private double lastYChange;
    private int vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(getData().isServerPos()) return;
        val move = getData().getMovementProcessor();

        if(!MathUtils.approxEquals(0.02, move.getClientYAcceleration(), move.getServerYAcceleration()) && !MiscUtils.cancelForFlight(getData(), 10, true)) {
            if(vl++ > 3) {
                flag("delta=" + MathUtils.getDelta(move.getClientYAcceleration(), move.getServerYAcceleration()), true, true, AlertTier.HIGH);
            }
        } else vl-= vl > 0 ? 1 : 0;
    }


    @Override
    public void onBukkitEvent(Event event) {

    }
}
