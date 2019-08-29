package cc.funkemunky.anticheat.impl.checks.movement.velocity;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.MathUtils;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {Packet.Client.POSITION, Packet.Client.POSITION_LOOK, Packet.Client.LOOK, Packet.Client.FLYING})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "Velocity (Type C)", description = "Checks for horizontal velocity modifications.", type = CheckType.VELOCITY, maxVL = 20, executable = true)
public class VelocityC extends Check {

    private float vl;
    private boolean didShit;
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();
        val velocity = getData().getVelocityProcessor();

        val deltaTicks = MathUtils.millisToTicks(timeStamp - velocity.getLastVelocityTimestamp());
        val pingTicks = MathUtils.millisToTicks(getData().getTransPing());

        boolean fromYInt = move.getFrom().getY() % 1 == 0;
        if(((deltaTicks == pingTicks && move.getDeltaY() > 0) || deltaTicks == pingTicks + 1) && (fromYInt || move.isServerOnGround()) && !move.isBlocksOnTop() && !didShit) {
            val ratio = move.getDeltaY() / (float)velocity.getVelocityY();
            val pct = ratio * 100;

            if(pct < 99.99) {
                if(vl++ > 2) {
                    flag("pct=" + MathUtils.round(pct, 2) + "% predicted=" + velocity.getVelocityY() + " vl=" + vl, true, true, AlertTier.HIGH);
                }
            } else vl-= vl > 0 ? 1 : 0;
            debug("velocityY=" + velocity.getVelocityY() + " deltaY=" + move.getDeltaY() + " pct=" + pct + "% vl=" + vl);
            didShit = true;
        } else didShit = false;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}