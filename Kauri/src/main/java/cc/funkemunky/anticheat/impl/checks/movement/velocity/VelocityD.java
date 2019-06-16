package cc.funkemunky.anticheat.impl.checks.movement.velocity;

import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MathUtils;
import lombok.val;
import org.bukkit.event.Event;

//@Init
@CheckInfo(name = "Velocity (Type D)", type = CheckType.VELOCITY, developer = true)
@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.POSITION, Packet.Client.LEGACY_POSITION_LOOK, Packet.Client.LEGACY_POSITION})
public class VelocityD extends Check {

    private int vl;
    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();
        val vel = getData().getVelocityProcessor();

        val vxz = MathUtils.hypot(vel.getMotionZ(), vel.getMotionX());

        if(vxz > 0 && !move.isServerOnGround()) {
            val percent = (float) (move.getDeltaXZ() / vxz) * 100;

            if(percent < 95) {
                vl++;
            } else vl-= vl > 0 ? 1 : 0;

            debug("percent=" + percent + "%" + " vl=" + vl);
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
