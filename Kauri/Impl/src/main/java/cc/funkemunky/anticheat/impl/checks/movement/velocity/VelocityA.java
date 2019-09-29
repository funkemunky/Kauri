package cc.funkemunky.anticheat.impl.checks.movement.velocity;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutEntityMetadata;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MathUtils;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {
        Packet.Client.POSITION_LOOK,
        Packet.Client.POSITION,
        Packet.Client.FLYING,
        Packet.Client.LOOK,
        Packet.Server.ENTITY_VELOCITY})
@Init
@CheckInfo(name = "Velocity (Type A)", description = "Detects any vertical velocity modification below 100%.",
        type = CheckType.VELOCITY, maxVL = 40, executable = true)
public class VelocityA extends Check {

    private float vl;
    private int ticks;
    private double vY;
    private boolean didCheck;
    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(packetType.equals(Packet.Server.ENTITY_VELOCITY)) {
            WrappedOutVelocityPacket velocity = new WrappedOutVelocityPacket(packet, getData().getPlayer());

            if(velocity.getId() == getData().getPlayer().getEntityId()
                    && getData().getMovementProcessor().isServerOnGround()) {
                vY = velocity.getY();
            }
        } else {
            val move = getData().getMovementProcessor();
            if(vY > 0 && getData().getVelocityProcessor().getLastVelocity().hasNotPassed(6)) {
                if(!move.isBlocksOnTop()
                        && !move.isServerOnGround()
                        && !move.isServerPos()) {
                    double deltaY = move.getTo().getY() - move.getFrom().getY();
                    double pct = deltaY / vY * 100;

                    if(pct < 99.999) {
                        if(vl++ > 8) {
                            flag("pct=" + MathUtils.round(pct, 3), true, true, AlertTier.HIGH);
                        }
                    } else vl-= vl > 0 ? 0.2f : 0;

                    debug("pct=" + pct);

                    vY -= 0.08;
                    vY *= 0.98;
                    didCheck = true;
                }
            } else if(didCheck) {
                didCheck = false;
                vY = 0;
            }
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
