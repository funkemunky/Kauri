package cc.funkemunky.anticheat.impl.checks.movement.velocity;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import cc.funkemunky.api.utils.MathUtils;
import lombok.val;
import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.List;

@Packets(packets = {Packet.Client.POSITION, Packet.Client.POSITION_LOOK, Packet.Client.LEGACY_POSITION_LOOK, Packet.Client.LEGACY_POSITION, Packet.Server.ENTITY_VELOCITY})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "Velocity (Type B)", description = "Checks for horizontal velocity modifications.", type = CheckType.VELOCITY, maxVL = 80, executable = false)
public class VelocityB extends Check {

    private double vl, velocityX, velocityZ;
    private int ticks;
    private List<Float> offsets = new ArrayList<>();

    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(packetType.equalsIgnoreCase(Packet.Server.ENTITY_VELOCITY)) {
            WrappedOutVelocityPacket dy = new WrappedOutVelocityPacket(packet, getData().getPlayer());
            if(dy.getId() == getData().getPlayer().getEntityId()) {
                velocityX = dy.getX();
                velocityZ = dy.getZ();
            }
        } else if(velocityX != 0.0D && velocityZ != 0.0D && ticks++ >= MiscUtils.millisToTicks(getData().getPing())) {
            val move = getData().getMovementProcessor();
            double dy = move.getTo().getY() - move.getFrom().getY();
            if(dy > 0D && !getData().isServerPos()) {
                float aimove = Atlas.getInstance().getBlockBoxManager().getBlockBox().getAiSpeed(getData().getPlayer()) * 1.175f;
                double velocityXZ = MathUtils.hypot(velocityX, velocityZ);
                if(move.getBlockAboveTicks() == 0 && move.getLiquidTicks() == 0 && move.getWebTicks() == 0 && velocityXZ > 0.15 && !move.isBlocksNear()) {

                    offsets.add(move.getDeltaXZ() * (getData().getLastAttack().hasNotPassed(3) ? 1.25f : 1));

                    if(offsets.size() >= 4) {
                        double average = offsets.stream().mapToDouble(val -> val).average().getAsDouble();
                        double quotient =  average / velocityXZ;
                        double threshold = (1 - aimove) / (getData().getLastAttack().hasNotPassed(3) ? 2.2f : 1.8);

                        if(quotient < threshold) {
                            if(vl++ > 8) {
                                flag("quotient=" + quotient + " threshold=" + threshold + " avg=" + average, true, true, AlertTier.HIGH);
                            }
                        } else vl-= vl > 0 ? 1 : 0;
                        debug("q=" + quotient + "/" + threshold + " vl=" + vl + " vel=" + velocityXZ + " dxz=" + average);
                        offsets.clear();
                        velocityX = velocityZ = ticks = 0;
                    }
                }
            }
        }

    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}