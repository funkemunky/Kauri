package cc.funkemunky.anticheat.impl.checks.movement.velocity;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
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

    //Skidded from GCheat for now.

    private double vl, velocityX, velocityZ;
    private int ticks;

    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();
        if(packetType.equalsIgnoreCase(Packet.Server.ENTITY_VELOCITY)) {
            WrappedOutVelocityPacket dy = new WrappedOutVelocityPacket(packet, getData().getPlayer());
            if(dy.getId() == getData().getPlayer().getEntityId() && move.isServerOnGround()) {
                velocityX = dy.getX();
                velocityZ = dy.getZ();
            }
        } else if((velocityX != 0 || velocityZ != 0) && (ticks > 0 || move.getFrom().getY() % 1 == 0) && !move.isBlocksNear() && !move.isBlocksOnTop() && move.getLiquidTicks() == 0 && move.getWebTicks() == 0 &&
                move.getDeltaY() > 0.0) {

            if(getData().getLastAttack().hasNotPassed(0)) {
                velocityX*= 0.6;
                velocityZ*= 0.6;
            }

            double velocityH = MathUtils.hypot(velocityX, velocityZ);

            double ratio = move.getDeltaXZ() / velocityH;
            if (ratio < 0.62) {
                if(vl++ > 8) {
                    flag(MathUtils.round(ratio * 100, 2) + "%", true, true, AlertTier.HIGH);
                }
            } else vl-= vl > 0 ? 1 : 0;
            debug("ratio=" + ratio + " vl=" + vl + " ticks=" + ticks);

            if(ticks++ > 1) {
                velocityX = velocityZ = ticks = 0;
            } else {
                velocityX/= 1.87728937729;
                velocityZ/= 1.87728937729;
            }
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}