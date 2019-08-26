package cc.funkemunky.anticheat.impl.checks.movement.velocity;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutEntityMetadata;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MathUtils;
import lombok.val;
import org.bukkit.event.Event;

import java.math.RoundingMode;

//TODO Recode this shit too
@Packets(packets = {
        Packet.Client.POSITION,
        Packet.Client.POSITION_LOOK,
        Packet.Client.LOOK,
        Packet.Client.FLYING
        , Packet.Server.ENTITY_VELOCITY})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "Velocity (Type B)", description = "Checks for horizontal velocity modifications.", type = CheckType.VELOCITY, maxVL = 20)
public class VelocityB extends Check {

    private double vl, velocityX, velocityZ;
    private long velocityTimestamp;

    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(packetType.equals(Packet.Server.ENTITY_VELOCITY)) {
            WrappedOutVelocityPacket velocity = new WrappedOutVelocityPacket(packet, getData().getPlayer());

            if(velocity.getId() == getData().getPlayer().getEntityId()) {
                velocityX = velocity.getX();
                velocityZ = velocity.getZ();
                velocityTimestamp = timeStamp;
            }
        } else {
            val move = getData().getMovementProcessor();
            val velocity = getData().getVelocityProcessor();

            long delta = timeStamp - velocity.getLastVelocityTimestamp(), ping = getData().getTransPing();
            long deltaTicks = MathUtils.millisToTicks(delta), pingTicks = MathUtils.millisToTicks(ping);
            float predicted = (float) MathUtils.hypot(velocity.getVelocityX(), velocity.getVelocityZ());
            if(deltaTicks == pingTicks && (velocityX != 0 || velocityZ != 0)) {
                if(!getData().isLagging()) {
                    debug(Color.Green + "ping=" + ping + " delta=" + delta + " predicted=" + predicted + " deltaXZ=" + move.getDeltaXZ());
                }
                velocityX = velocityZ = 0;
            }
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }

    private long millisToTicks(long millis) {
        return (long) Math.ceil(millis / 50D);
    }
}