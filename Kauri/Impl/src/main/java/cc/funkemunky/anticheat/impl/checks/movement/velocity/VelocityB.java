package cc.funkemunky.anticheat.impl.checks.movement.velocity;

import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MathUtils;
import lombok.val;
import org.bukkit.event.Event;

//TODO Recode this shit too
@Packets(packets = {
        Packet.Client.POSITION,
        Packet.Client.POSITION_LOOK,
        Packet.Client.LOOK,
        Packet.Client.FLYING,
        Packet.Server.ENTITY_VELOCITY})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "Velocity (Type B)", description = "Checks for horizontal velocity modifications.", type = CheckType.VELOCITY, maxVL = 20)
public class VelocityB extends Check {

    private double vl, velocityX, velocityZ, velocityY;
    private long velocityTimestamp, lagTime;

    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(packetType.equals(Packet.Server.ENTITY_VELOCITY)) {
            WrappedOutVelocityPacket velocity = new WrappedOutVelocityPacket(packet, getData().getPlayer());

            if(velocity.getId() == getData().getPlayer().getEntityId()) {
                velocityX = velocity.getX();
                velocityY = velocity.getY();
                velocityZ = velocity.getZ();
                velocityTimestamp = timeStamp;
                debug("Sent velocity (" + System.currentTimeMillis() + ") [" + velocityX + ", " + velocityY + ", " + velocityZ + "]");
            }
        } else {
            val move = getData().getMovementProcessor();
            val velocity = getData().getVelocityProcessor();

            long delta = timeStamp - velocityTimestamp, ping = getData().getTransPing();
            long deltaTicks = MathUtils.millisToTicks(delta), pingTicks = MathUtils.millisToTicks(ping);
            //TODO Debug proper dividend.

            if(MathUtils.approxEquals(0.01, velocityY, move.getDeltaY()) && velocityY > 0) {
                float valueX = (float)(velocityX - move.getLastDeltaX());
                float valueZ = (float)(velocityZ - move.getLastDeltaZ());

                float predicted = (float)MathUtils.hypot(valueX, valueZ);
                float ratio = move.getDeltaXZ() / predicted, pct = ratio * 100;
                debug(move.getDeltaX() + ", " + move.getDeltaY() + ", " + move.getDeltaZ() + " deltaXZ=" + move.getDeltaXZ());
                //debug("predicted=" + predicted + " deltaXZ=" + move.getDeltaXZ() + " pct=" + pct);
                velocityX = velocityY = velocityZ = 0;
            }
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }

    private long millisToTicks(long millis) {
        return (long) Math.ceil(millis / 50D);
    }
}