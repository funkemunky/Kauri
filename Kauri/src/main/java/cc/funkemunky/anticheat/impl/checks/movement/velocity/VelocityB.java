package cc.funkemunky.anticheat.impl.checks.movement.velocity;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import cc.funkemunky.api.utils.MathUtils;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {Packet.Client.POSITION, Packet.Client.POSITION_LOOK, Packet.Client.LEGACY_POSITION_LOOK, Packet.Client.LEGACY_POSITION, Packet.Server.ENTITY_VELOCITY})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "Velocity (Type B)", description = "Checks for horizontal velocity modifications.", type = CheckType.VELOCITY, maxVL = 80, executable = false, developer = true)
public class VelocityB extends Check {

    private double vl, velocityX, velocityZ;
    private int ticks;

    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(packetType.equalsIgnoreCase(Packet.Server.ENTITY_VELOCITY)) {
            WrappedOutVelocityPacket dy = new WrappedOutVelocityPacket(packet, getData().getPlayer());
            if(dy.getId() == getData().getPlayer().getEntityId() && getData().getMovementProcessor().getFrom().getY() % 1.0D == 0.0D && getData().getMovementProcessor().isClientOnGround()) {
                velocityX = dy.getX();
                velocityZ = dy.getZ();
            }
        } else if(velocityX != 0.0D && velocityZ != 0.0D) {
            val move = getData().getMovementProcessor();
            double dy = move.getTo().getY() - move.getFrom().getY();
            if(dy > 0D) {
                float aimove = Atlas.getInstance().getBlockBoxManager().getBlockBox().getAiSpeed(getData().getPlayer()) * 1.175f;
                double velocityXZ = MathUtils.hypot(velocityX, velocityZ) / (ticks > 0 ? 1.96 : 1) - (ticks * 0.026f);
                if(move.getBlockAboveTicks() == 0 && move.getLiquidTicks() == 0 && move.getWebTicks() == 0 && velocityXZ > 0.15 && !move.isBlocksNear()) {
                    double quotient =  move.getDeltaXZ() / velocityXZ;
                    double threshold = (1 - aimove) / (getData().getLastAttack().hasNotPassed(1) ? 1.95 + (ticks * 0.025) : 1 + (ticks * 0.026));
                    if(quotient < threshold) {
                        if(vl++ >= 14.0D) {
                            flag("velocity: " + MathUtils.round(quotient * 100.0D, 1) + "%", true, true, AlertTier.LIKELY);
                        }
                    } else {
                        vl -= vl > 0 ? 1.2 / (ticks < 1 ? 4 : 1) : 0;
                    }

                    debug("q=" + quotient + "/" + threshold + " vl=" + vl + " vel=" + velocityXZ + " dxz=" + move.getDeltaXZ());
                }

                if(ticks++ == 3 || move.isServerOnGround()) {
                    velocityX = velocityZ = 0.0D;
                    ticks = 0;
                }
            }
        }

    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}