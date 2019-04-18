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

@Packets(packets = {Packet.Client.POSITION, Packet.Client.POSITION_LOOK, Packet.Client.LEGACY_POSITION_LOOK, Packet.Client.LEGACY_POSITION, Packet.Server.ENTITY_VELOCITY})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "Velocity (Type B)", description = "Checks for horizontal velocity modifications.", type = CheckType.VELOCITY, maxVL = 80, executable = false)
public class VelocityB extends Check {

    private double vl, velocityX, velocityZ;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(packetType.equals(Packet.Server.ENTITY_VELOCITY)) {
            WrappedOutVelocityPacket velocity = new WrappedOutVelocityPacket(packet, getData().getPlayer());

            if(velocity.getId() == getData().getPlayer().getEntityId() && getData().getMovementProcessor().isClientOnGround()) {
                velocityX = velocity.getX();
                velocityZ = velocity.getZ();
            }
        } else if(velocityX != 0 && velocityZ != 0) {
            val dy = getData().getMovementProcessor().getTo().getY() - getData().getMovementProcessor().getFrom().getY();

            if(dy < 0.419 && dy > 0.1) {
                val dxz = Math.hypot(getData().getMovementProcessor().getTo().getX() - getData().getMovementProcessor().getFrom().getX(),
                        getData().getMovementProcessor().getTo().getZ() - getData().getMovementProcessor().getFrom().getZ());

                val kbxz = Math.hypot(velocityX, velocityZ);

                //the only accurate way to check horizontal kb is to check it in the air, if the player is on ground it won't work
                //people might say this is from agc or whatever but its from gcheat, just like entire agc is (no joke)
                val aimove = Atlas.getInstance().getBlockBoxManager().getBlockBox().getAiSpeed(getData().getPlayer()) * 2.9;
                if (getData().getMovementProcessor().getBlockAboveTicks() == 0
                        && getData().getMovementProcessor().getLiquidTicks() == 0
                        && getData().getMovementProcessor().getWebTicks() == 0
                        && kbxz > 0.15
                        && !getData().getMovementProcessor().isBlocksNear()) {

                    val quotient = dxz / kbxz;

                    val threshold = 0.7;

                    if (quotient < threshold) {
                        if (vl++ >= 14.0) {
                            flag("velocity: " + MathUtils.round(quotient * 100, 1) + "%", true, true);
                        }
                    } else {
                        vl = Math.max(0, vl - 1.5);
                    }

                    debug("QUOTIENT: " + quotient + "/" + threshold + " VL: " + vl);
                }

                velocityX = velocityZ = 0;
                //debug("KBXZ: " + kbxz + " DXZ: " + dxz + " AI: " + aimove);
            }
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
