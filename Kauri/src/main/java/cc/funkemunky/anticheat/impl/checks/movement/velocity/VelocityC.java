package cc.funkemunky.anticheat.impl.checks.movement.velocity;

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
@CheckInfo(name = "Velocity (Type C)", type = CheckType.VELOCITY, developer = true)
@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.POSITION, Packet.Client.LEGACY_POSITION_LOOK, Packet.Client.LEGACY_POSITION})
public class VelocityC extends Check {

    private int vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();
        val vel = getData().getVelocityProcessor();

        if(vel.getMotionY() > 0 && move.getDeltaY() > 0) {
            val percent = (move.getDeltaY() / vel.getMotionY()) * 100;

            if(percent < 99.8 && !move.isBlocksOnTop() && move.getAirTicks() < 12) {
                if(vl++ > 2) {
                    flag("velocity=" + percent, true, true, AlertTier.HIGH);
                }
            } else vl-= vl > 0 ? 1 : 0;

            debug("percent=" + percent + "%");
        }

        debug(move.getDeltaY() + ", " + vel.getMotionY());
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
