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
@CheckInfo(name = "Velocity (Type B)", description = "Checks for horizontal velocity modifications.", type = CheckType.VELOCITY, maxVL = 20)
public class VelocityB extends Check {

    //Skidded from GCheat for now.

    private double vl, velocityX, velocityZ, lastDeltaXZ;
    private int ticks;
    private boolean shit;

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
        } else if((velocityX != 0 || velocityZ != 0) && (shit || (move.getFrom().getY() % 1 == 0 && move.getDeltaY() > 0)) && !move.isBlocksNear() && !move.isBlocksOnTop() && move.getLiquidTicks() == 0 && move.getWebTicks() == 0) {

            if(!shit) {
                lastDeltaXZ = move.getLastDeltaXZ();
            }
            if(!move.isServerOnGround() && move.getDeltaY() > 0) {
                double velocityH = MathUtils.hypot(velocityX, velocityZ) - Math.min(0.4, shit ? lastDeltaXZ * 0.35F : 0);

                double ratio = move.getDeltaXZ() / velocityH;
                if (ratio < 0.63) {
                    if(vl++ > 8) {
                        double pct = MathUtils.round(ratio * 100, 2);
                        flag(pct + "%" + " or" + (pct + 37) + "% [vl=" + vl + "]", true, true, AlertTier.LIKELY);
                    }
                    //TODO Test this change from 0.5 to 0.2 for false positives.
                } else vl-= vl > 0 ? 0.2 : 0;
                debug("ratio=" + ratio + " vl=" + vl + " lastDelta=" + lastDeltaXZ + " deltaxz=" + move.getDeltaXZ() + " vel=" + velocityH);
            } else {
                velocityZ = velocityX = 0;
                shit = false;
            }

            if(!shit) {
                velocityX/= 1.85;
                velocityZ/= 1.85;
                shit = true;
            } else {
                velocityX = velocityZ = 0;
                shit = false;
            }
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}