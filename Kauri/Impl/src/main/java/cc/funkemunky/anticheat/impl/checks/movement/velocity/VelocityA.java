package cc.funkemunky.anticheat.impl.checks.movement.velocity;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.SkiddedUtils;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutEntityMetadata;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MathUtils;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {
        Packet.Client.POSITION_LOOK,
        Packet.Client.POSITION,
        Packet.Client.FLYING,
        Packet.Client.LOOK,
        Packet.Server.ENTITY_VELOCITY})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "Velocity (Type A)", description = "Detects any vertical velocity modification below 100%.", type = CheckType.VELOCITY, maxVL = 40, executable = true)
public class VelocityA extends Check {

    private float velocityY;
    private long lastVelocity;
    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(packetType.equalsIgnoreCase(Packet.Server.ENTITY_VELOCITY)) {
            WrappedOutVelocityPacket velocity = new WrappedOutVelocityPacket(packet, getData().getPlayer());

            if(velocity.getY() != 0 && velocity.getId() == getData().getPlayer().getEntityId()) {
                velocityY = (float) velocity.getY();
                lastVelocity = timeStamp;
            }
        } else {
            val move = getData().getMovementProcessor();

            val ping = getData().getTransPing();
            long delta = System.currentTimeMillis() - getData().getVelocityProcessor().getLastVelocityTimestamp();

            long pingTicks = MathUtils.millisToTicks(ping), deltaTicks = MathUtils.millisToTicks(delta);
            /*if(deltaTicks == pingTicks) {
                debug(Color.Green + "ping=" + ping + " timeDelta=" + delta + " deltaY=" + move.getDeltaY() + " velocityY=" + getData().getVelocityProcessor().getVelocityY());
            } else if(MathUtils.approxEquals(2, MathUtils.millisToTicks(delta), MathUtils.millisToTicks(ping))) {

                debug("deltaY=" + move.getDeltaY() + " velocityY=" + getData().getVelocityProcessor().getVelocityY() + " motY=" + getData().getVelocityProcessor().getMotionY() + " pingTicks=" + pingTicks + " deltaTicks=" + deltaTicks);
            }*/

            if(deltaTicks >= pingTicks && MathUtils.approxEquals(2, MathUtils.millisToTicks(delta), MathUtils.millisToTicks(ping))) {
                if(!MathUtils.approxEquals(0.01, getData().getVelocityProcessor().getMotionY(), move.getDeltaY())) {
                    debug(Color.Green + "Flag: " + getData().getVelocityProcessor().getMotionY() + ", " + move.getDeltaY());
                }
            }
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
