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
@CheckInfo(name = "Fly (Type F)", description = "Checks for impossibly low acceleration changes.", type = CheckType.FLY, maxVL = 40)
@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.POSITION})
public class FlyF extends Check {
    private float vl;
    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        if(move.isCancelFlight()) {
            vl-= vl > 0 ? 0.05 : 0;
            return;
        }

        if(Math.abs(move.getClientYAcceleration()) < 0.01 && !move.isServerOnGround()) {
            if(vl++ > 5) {
                flag("accel=" + move.getClientYAcceleration() + " vl=" + vl, true, true, AlertTier.HIGH);
            }
        } else vl-= vl > 0 ? 0.5 : 0;

        debug("accel=" + move.getClientYAcceleration() + " deltaY=" + move.getDeltaY() + " vl=" + vl + " onGround=" + move.isServerOnGround());
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
