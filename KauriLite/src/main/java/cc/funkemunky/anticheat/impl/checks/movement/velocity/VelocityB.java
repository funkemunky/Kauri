package cc.funkemunky.anticheat.impl.checks.movement.velocity;

import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Setting;
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
    private int ticks;

    @Setting(name = "threshold.vl.max")
    private double vlMax = 14D;

    @Setting(name = "threshold.vl.add")
    private double add = 1D;

    @Setting(name = "threshold.vl.subtract")
    private double subtract = 0.55D;

    @Setting(name = "multipliers.airMove")
    private float airMove = 1.08f;

    @Setting(name = "multipliers.attack")
    private float attackMult = 1.96f;

    @Setting(name = "multipliers.direction")
    private float directionMult = 7.35f;

    @Setting(name = "velocityTicks")
    private int maxTicks = 1;

    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();
        if(packetType.equalsIgnoreCase(Packet.Server.ENTITY_VELOCITY)) {
            WrappedOutVelocityPacket vel = new WrappedOutVelocityPacket(packet, getData().getPlayer());
            if(vel.getId() == getData().getPlayer().getEntityId() && move.getFrom().getY() % 1.0D == 0.0D && move.isClientOnGround()) {
                velocityX = vel.getX();
                velocityZ = vel.getZ();
            }
        } else if(velocityX != 0.0D && velocityZ != 0.0D) {
            val dy = move.getTo().getY() - move.getFrom().getY();
            if(dy > 0) {
                val action = getData().getActionProcessor();
                double dx = move.getTo().getX() - move.getFrom().getX(), dz = move.getTo().getZ() - move.getFrom().getZ();
                Vector kb = new Vector(velocityX, 0, velocityZ), d = new Vector(dx, 0, dz);
                float aimove = Atlas.getInstance().getBlockBoxManager().getBlockBox().getAiSpeed(getData().getPlayer());
                if(move.getBlockAboveTicks() == 0 && move.getLiquidTicks() == 0 && move.getWebTicks() == 0 && !move.isBlocksNear()) {
                    double kbxz = kb.length() / (ticks > 0 ? 1.8 : 1) - (0.026f * ticks), dxz = d.length();

                    val quotient = dxz / kbxz;
                    val directionDelta = 1.85 - kb.distance(move.getTo().toLocation(getData().getPlayer().getWorld()).getDirection());
                    val threshold = (0.88 - aimove * airMove) / (getData().getLastAttack().hasNotPassed(0) ? attackMult : 1) + (directionDelta / directionMult);
                    if(quotient < threshold) {
                        if((vl+= add) > vlMax) {
                            flag("velocity: " + MathUtils.round(quotient * 100.0D, 1) + "%", true, true);
                        }
                    } else {
                        vl = Math.max(0.0D, vl - subtract);
                    }

                    debug("QUOTIENT: " + quotient + "/" + threshold + " VL: " + vl + " dy=" + dy + "  delta=" + directionDelta + " dxz=" + dxz + " kbxz=" + kbxz);
                }

                velocityX = velocityZ = 0.0D;
            }
        }

    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
