package cc.funkemunky.anticheat.impl.checks.movement.velocity;

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
import org.bukkit.util.Vector;

@Packets(packets = {Packet.Client.POSITION, Packet.Client.POSITION_LOOK, Packet.Client.LEGACY_POSITION_LOOK, Packet.Client.LEGACY_POSITION, Packet.Server.ENTITY_VELOCITY})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "Velocity (Type B)", description = "Checks for horizontal velocity modifications.", type = CheckType.VELOCITY, maxVL = 80, executable = false)
public class VelocityB extends Check {

    private double vl, velocityX, velocityZ;

    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(packetType.equalsIgnoreCase(Packet.Server.ENTITY_VELOCITY)) {
            WrappedOutVelocityPacket id = new WrappedOutVelocityPacket(packet, this.getData().getPlayer());
            if(dy.getId() == this.getData().getPlayer().getEntityId() && this.getData().getMovementProcessor().getFrom().getY() % 1.0D == 0.0D && this.getData().getMovementProcessor().isClientOnGround()) {
                this.velocityX = dy.getX();
                this.velocityZ = dy.getZ();
            }
        } else if(this.velocityX != 0.0D && this.velocityZ != 0.0D) {
            double dy = this.getData().getMovementProcessor().getTo().getY() - this.getData().getMovementProcessor().getFrom().getY();
            if(dy < 0.419D && dy > 0.1D) {
                double dx = this.getData().getMovementProcessor().getTo().getX() - this.getData().getMovementProcessor().getFrom().getX(), dz = this.getData().getMovementProcessor().getTo().getZ() - this.getData().getMovementProcessor().getFrom().getZ();
                Vector kb = new Vector(this.velocityX, 0, this.velocityZ), dxz = new Vector(dx, 0, dz);
                float aimove = Atlas.getInstance().getBlockBoxManager().getBlockBox().getAiSpeed(this.getData().getPlayer());
                if(this.getData().getMovementProcessor().getBlockAboveTicks() == 0 && this.getData().getMovementProcessor().getLiquidTicks() == 0 && this.getData().getMovementProcessor().getWebTicks() == 0 && kb.length() > 0.15 && !this.getData().getMovementProcessor().isBlocksNear()) {
                    double quotient = 1 - kb.distance(dxz);
                    double threshold = 1 - ((double)aimove + (this.getData().getLastAttack().hasNotPassed(0)?-0.05D:0.0D));
                    if(quotient < threshold) {
                        if(this.vl++ >= 14.0D) {
                            this.flag("velocity: " + MathUtils.round(quotient * 100.0D, 1) + "%", true, true);
                        }
                    } else {
                        this.vl = Math.max(0.0D, this.vl - 0.75D);
                    }

                    this.debug("QUOTIENT: " + quotient + "/" + threshold + " VL: " + this.vl + " y=" + dy + " ai=" + aimove);
                }

                this.velocityX = this.velocityZ = 0.0D;
            }
        }

    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
