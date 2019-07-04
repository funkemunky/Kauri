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

@Packets(packets = {Packet.Client.POSITION, Packet.Client.POSITION_LOOK, Packet.Client.USE_ENTITY, Packet.Server.ENTITY_VELOCITY})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "Velocity (Type C)", description = "Checks for horizontal velocity modifications.", type = CheckType.VELOCITY, maxVL = 10)
public class VelocityC extends Check {

    private double vl, velocityX, velocityZ;

    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();
        if(packetType.equalsIgnoreCase(Packet.Server.ENTITY_VELOCITY)) {
            WrappedOutVelocityPacket dy = new WrappedOutVelocityPacket(packet, getData().getPlayer());
            if(dy.getId() == getData().getPlayer().getEntityId() && move.isServerOnGround()) {
                velocityX = dy.getX();
                velocityZ = dy.getZ();
            }
        } else if(packetType.equals(Packet.Client.USE_ENTITY)) {
            velocityX*= 0.6;
            velocityZ*= 0.6;
        } else if((velocityX != 0 || velocityZ != 0) && move.getDeltaY() > 0 && move.getFrom().getY() % 1 == 0 && !move.isBlocksNear() && !move.isBlocksOnTop() && move.getLiquidTicks() == 0 && move.getWebTicks() == 0) {

            double velocityH = MathUtils.hypot(velocityX, velocityZ);

            double ratio = move.getDeltaXZ() / velocityH;
            if (ratio < 0.62) {
                if(vl++ > 8) {
                    flag(MathUtils.round(ratio * 100, 2) + "%", true, true, AlertTier.HIGH);
                }
            } else vl-= vl > 0 ? 0.45 : 0;
            debug("ratio=" + ratio + " vl=" + vl + " attack=" + getData().getLastAttack().getPassed());

            velocityX = velocityZ = 0;
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}