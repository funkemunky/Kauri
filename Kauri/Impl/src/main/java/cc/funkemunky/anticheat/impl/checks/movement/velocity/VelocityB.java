package cc.funkemunky.anticheat.impl.checks.movement.velocity;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MathUtils;
import lombok.val;
import org.bukkit.event.Event;

@CheckInfo(name = "Velocity (Type B)", description = "Checks to see if the horizontal velocity of a player is legitimate.",
        type = CheckType.VELOCITY, developer = true)
@Init
@Packets(packets = {Packet.Client.POSITION, Packet.Client.POSITION_LOOK, Packet.Client.LOOK,
        Packet.Client.FLYING, Packet.Server.ENTITY_VELOCITY, Packet.Client.USE_ENTITY})
public class VelocityB extends Check {

    private double vX, vZ;
    private float vl;
    private boolean didCheck;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(packetType.equals(Packet.Server.ENTITY_VELOCITY)) {
            WrappedOutVelocityPacket velocity = new WrappedOutVelocityPacket(packet, getData().getPlayer());

            if(velocity.getId() == getData().getPlayer().getEntityId()
                    && !getData().getMovementProcessor().isServerOnGround()) {
                vX = velocity.getX();
                vZ = velocity.getZ();
            }
        } else if(packetType.equalsIgnoreCase(Packet.Client.USE_ENTITY)) {
            vX*= 0.6f;
            vZ*= 0.6f;
        } else {
            val move = getData().getMovementProcessor();

            if((vX != 0 || vZ != 0)
                    && getData().getVelocityProcessor().getLastVelocity().hasNotPassed(3)
                    && !move.isServerOnGround()) {
                double dX, dZ;
                if(getData().getVelocityProcessor().getLastVelocity().getPassed() == 0) {
                    dX = move.getLastDeltaX() - move.getDeltaX();
                    dZ = move.getLastDeltaZ() - move.getDeltaZ();
                } else {
                    dX = move.getDeltaX();
                    dZ = move.getDeltaZ();
                }

                double deltaXZ = MathUtils.hypot(dX, dZ);
                double vXZ = MathUtils.hypot(vX, vZ);
                double pct = deltaXZ / vXZ * 100;

                if(pct < 85) {
                    if(vl++ > 20) {
                        flag("pct=" + MathUtils.round(pct, 2),
                                true, true, AlertTier.HIGH);
                    } else if(vl > 12) {
                        flag("pct=" + MathUtils.round(pct, 2),
                                true, true, AlertTier.LIKELY);
                    }
                } else vl-= vl > 0 ? 0.5f : 0;

                debug("ticks:" + getData().getVelocityProcessor().getLastVelocity().getPassed()
                        + " pct=" + pct + " vl=" + vl);
                vX*= 0.91;
                vZ*= 0.91;
                didCheck = true;
            } else if(didCheck || move.isServerOnGround()) {
                vX = vZ = 0;
                didCheck = false;
            }
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
