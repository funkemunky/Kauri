package cc.funkemunky.anticheat.impl.checks.movement.velocity;

import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import cc.funkemunky.api.utils.MathUtils;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

@Packets(packets = {Packet.Client.POSITION, Packet.Client.POSITION_LOOK, Packet.Client.LEGACY_POSITION_LOOK, Packet.Client.LEGACY_POSITION, Packet.Server.ENTITY_VELOCITY})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "Velocity (Type B)", description = "Checks for horizontal velocity modifications.", type = CheckType.VELOCITY, maxVL = 80, executable = false)
public class VelocityB extends Check {

    private double vl, velocityX, velocityZ;

    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(packetType.equalsIgnoreCase(Packet.Server.ENTITY_VELOCITY)) {
            WrappedOutVelocityPacket dy = new WrappedOutVelocityPacket(packet, getData().getPlayer());
            if(dy.getId() == getData().getPlayer().getEntityId() && getData().getMovementProcessor().getFrom().getY() % 1.0D == 0.0D && getData().getMovementProcessor().isClientOnGround()) {
                velocityX = dy.getX();
                velocityZ = dy.getZ();
            }
        } else if(velocityX != 0.0D && velocityZ != 0.0D) {
            double dy = getData().getMovementProcessor().getTo().getY() - getData().getMovementProcessor().getFrom().getY();
            if(dy < 0.419D && dy > 0.1D) {
                double dx = getData().getMovementProcessor().getTo().getX() - getData().getMovementProcessor().getFrom().getX(), dz = getData().getMovementProcessor().getTo().getZ() - getData().getMovementProcessor().getFrom().getZ();
                Vector kb = new Vector(velocityX, 0, velocityZ), dxz = new Vector(dx, 0, dz);
                float aimove = Atlas.getInstance().getBlockBoxManager().getBlockBox().getAiSpeed(getData().getPlayer()) * 1.35f;
                if(getData().getMovementProcessor().getBlockAboveTicks() == 0 && getData().getMovementProcessor().getLiquidTicks() == 0 && getData().getMovementProcessor().getWebTicks() == 0 && kb.length() > 0.15 && !getData().getMovementProcessor().isBlocksNear()) {
                    double quotient = 1 - kb.distance(dxz);
                    double threshold = (1 - aimove / (getData().getLastAttack().hasNotPassed(0) ? 2 : 1));
                    if(quotient < threshold) {
                        if(vl++ >= 14.0D) {
                            flag("velocity: " + MathUtils.round(quotient * 100.0D, 1) + "%", true, true);
                        }
                    } else {
                        vl = Math.max(0.0D, vl - 0.75D);
                    }

                    debug("QUOTIENT: " + quotient + "/" + threshold + " VL: " + vl + " y=" + dy + " ai=" + aimove);
                }

                velocityX = velocityZ = 0.0D;
            }
        }

    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}