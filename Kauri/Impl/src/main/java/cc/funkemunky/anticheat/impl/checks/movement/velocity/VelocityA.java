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

@Packets(packets = {
        Packet.Server.ENTITY_VELOCITY,
        Packet.Client.POSITION_LOOK,
        Packet.Client.POSITION,
        Packet.Client.FLYING,
        Packet.Client.LEGACY_POSITION_LOOK,
        Packet.Client.LEGACY_POSITION})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "Velocity (Type A)", description = "Detects any vertical velocity modification below 100%.", type = CheckType.VELOCITY, maxVL = 40)
public class VelocityA extends Check {

    private float lastVelocity;
    private int vl, ticks;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();
        if (packetType.equals(Packet.Server.ENTITY_VELOCITY)) {
            WrappedOutVelocityPacket velocity = new WrappedOutVelocityPacket(packet, getData().getPlayer());

            if (velocity.getId() == velocity.getPlayer().getEntityId() && move.isClientOnGround()) {
                lastVelocity = (float) velocity.getY();
            }
        } else if (lastVelocity > 0 && move.getFrom().getY() % 1 == 0 && (move.getDeltaY() > 0 || ticks++ > MathUtils.millisToTicks(getData().getTransPing())))  {
            val ratio = Math.abs(move.getDeltaY() / lastVelocity);
            val percentage = MathUtils.round(ratio * 100D, 1);

            if (ratio < 1 && !move.isBlocksOnTop() && !getData().isLagging() && move.getLiquidTicks() < 1 && move.getClimbTicks() < 1 && !getData().getPlayer().getAllowFlight() && getData().getPlayer().getVehicle() == null) {
                if(vl++ > 10) {
                    flag("velocity: " + percentage + "%", true, true, AlertTier.CERTAIN);
                } else if(vl > 5) {
                    flag("velocity: " + percentage + "%", true, true, AlertTier.HIGH);
                } else if(vl > 2) {
                    flag("velocity: " + percentage + "%", true, true, AlertTier.POSSIBLE);
                }
            } else {
                vl -= vl > 0 ? 1 : 0;
            }

            debug("RATIO: " + ratio + " VL: " + vl + " DELTAY:" + (getData().getMovementProcessor().getTo().getY() - getData().getMovementProcessor().getFrom().getY()) + "VELOCITY: " + lastVelocity);

            lastVelocity = ticks = 0;
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
