package cc.funkemunky.anticheat.impl.checks.movement.velocity;

import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Setting;
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


    @Setting(name = "thresoldVL")
    private int maxVL = 7;

    private float lastVelocity;
    private int vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if (packetType.equals(Packet.Server.ENTITY_VELOCITY)) {
            WrappedOutVelocityPacket velocity = new WrappedOutVelocityPacket(packet, getData().getPlayer());

            if (velocity.getId() == velocity.getPlayer().getEntityId() && getData().getMovementProcessor().getFrom().getY() % 1 == 0 && getData().getMovementProcessor().isClientOnGround()) {
                lastVelocity = (float) velocity.getY();
            }
        }
        if (lastVelocity > 0 && getData().getMovementProcessor().getDeltaY() > 0) {
            val ratio = Math.abs(getData().getMovementProcessor().getDeltaY() / lastVelocity);
            val percentage = MathUtils.round(ratio * 100D, 1);

            if (ratio < 1 && !getData().getMovementProcessor().isBlocksOnTop() && !getData().isAbleToFly()) {
                if (vl++ > maxVL) {
                    flag("velocity: " + percentage + "%", true, true);
                }
            } else {
                vl -= vl > 0 ? 1 : 0;
            }

            debug("RATIO: " + ratio + " VL: " + vl + " DELTAY:" + (getData().getMovementProcessor().getTo().getY() - getData().getMovementProcessor().getFrom().getY()) + "VELOCITY: " + lastVelocity);

            lastVelocity = 0;
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
