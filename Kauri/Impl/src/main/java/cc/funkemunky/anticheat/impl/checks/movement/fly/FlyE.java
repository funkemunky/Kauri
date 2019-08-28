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

@Init
@CheckInfo(name = "Fly (Type E)", description = "Checks for impossibly low y motion over a period of time.", type = CheckType.FLY, maxVL = 15, executable = true)
@Packets(packets = {Packet.Client.POSITION, Packet.Client.POSITION_LOOK})
public class FlyE extends Check {

    private float vl;
    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        if(move.isCancelFlight()) {
            vl-= vl > 0 ? 0.05 : 0;
            return;
        }

        if(move.getDeltaY() < 1E-6 && !move.isServerOnGround()) {
            if(vl++ > 4) {
                flag("deltaY=" + move.getDeltaY() + " vl=" + vl, true, true, AlertTier.HIGH);
            }
        } else vl-= vl > 0 ? 0.5 : 0;

        debug("deltaY=" + move.getDeltaY() + " vl=" + vl + " onGround=" + move.isServerOnGround());
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
