package cc.funkemunky.anticheat.impl.checks.movement.velocity;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import cc.funkemunky.api.utils.MathUtils;
import lombok.val;
import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.List;

@Packets(packets = {Packet.Client.POSITION, Packet.Client.POSITION_LOOK, Packet.Client.LEGACY_POSITION_LOOK, Packet.Client.LEGACY_POSITION, Packet.Server.ENTITY_VELOCITY})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "Velocity (Type C)", description = "Checks for horizontal velocity modifications.", type = CheckType.VELOCITY, maxVL = 80, executable = false)
public class VelocityC extends Check {

    private double vl, velocityX, velocityZ;
    private int ticks, ticks2;

    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(packetType.equalsIgnoreCase(Packet.Server.ENTITY_VELOCITY)) {
            WrappedOutVelocityPacket dy = new WrappedOutVelocityPacket(packet, getData().getPlayer());
            if(dy.getId() == getData().getPlayer().getEntityId() && getData().getMovementProcessor().getFrom().getY() % 1 == 0 && getData().getMovementProcessor().isServerOnGround()) {
                velocityX = dy.getX();
                velocityZ = dy.getZ();
            }
        } else if((velocityX != 0 || velocityZ != 0)) {
            val move = getData().getMovementProcessor();
            double velocity = MathUtils.hypot(velocityX, velocityZ);
            float deltaXZ = move.getDeltaXZ();

            switch(ticks2) {
                case 1:
                    velocity/= 1.8f;
                    break;
                case 2: {
                    velocity/= 1.8f;
                    velocity*= 0.9f;
                    break;
                }
            }

            double threshold = velocity;
            float percent = (float) MathUtils.round(deltaXZ / threshold * 100, 2);

            if(percent < 85) {
                if(vl++ > 6) {
                    flag(percent + "<-80", false, true, AlertTier.HIGH);
                }
            } else vl-= vl > 0 ? 1 : 0;

            debug("xz=" + move.getDeltaXZ() + " vel=" + velocity + " pct=" + percent + " vl=" + vl);
            if(ticks2++ > 2) {
                velocityX = velocityZ = ticks2 = 0;
            }
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}