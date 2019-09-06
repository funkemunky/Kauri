package cc.funkemunky.anticheat.impl.checks.movement.velocity;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MathUtils;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {
        Packet.Client.POSITION_LOOK,
        Packet.Client.POSITION,
        Packet.Client.FLYING,
        Packet.Client.LOOK})
//@cc.funkemunky.api.utils.Init
@CheckInfo(name = "Velocity (Type A)", description = "Detects any vertical velocity modification below 100%.",
        type = CheckType.VELOCITY, maxVL = 40, executable = true)
public class VelocityA extends Check {

    private float vl;
    private int ticks;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        val ping = getData().getTransPing();
        long delta = timeStamp - getData().getVelocityProcessor().getLastVelocityTimestamp();

        long pingTicks = MathUtils.millisToTicks(ping), deltaTicks = MathUtils.millisToTicks(delta);
            /*if(deltaTicks == pingTicks) {
                debug(Color.Green + "ping=" + ping + " timeDelta=" + delta + " deltaY=" + move.getDeltaY() + " velocityY=" + getData().getVelocityProcessor().getVelocityY());
            } else if(MathUtils.approxEquals(2, MathUtils.millisToTicks(delta), MathUtils.millisToTicks(ping))) {

                debug("deltaY=" + move.getDeltaY() + " velocityY=" + getData().getVelocityProcessor().getVelocityY() + " motY=" + getData().getVelocityProcessor().getMotionY() + " pingTicks=" + pingTicks + " deltaTicks=" + deltaTicks);
            }*/

        //if this works, great! if not, use the one below since it is more likely to work from theory.
            /*if(deltaTicks >= pingTicks && MathUtils.approxEquals(2, deltaTicks, pingTicks)) {
                if(!MathUtils.approxEquals(0.01, getData().getVelocityProcessor().getMotionY(), move.getDeltaY())) {
                    debug(Color.Green + "Flag: " + getData().getVelocityProcessor().getMotionY() + ", " + move.getDeltaY());
                }
            }*/

        if (deltaTicks <= pingTicks) ticks = 0;

        long subtracted = deltaTicks - pingTicks;
        if (((move.getDeltaY() > 0 &&
                MathUtils.approxEquals(0.01, getData().getVelocityProcessor().getVelocityY(), move.getDeltaY())
                && deltaTicks >= pingTicks) || deltaTicks > pingTicks)
                && subtracted < 3
                && getData().getVelocityProcessor().getVelocityY() > 0) {
            ticks++;

            if (!getData().isLagging() && !move.isBlocksOnTop()) {
                float predicted = (float) getData().getVelocityProcessor().getVelocityY();

                for (long i = 1; i < ticks; i++) {
                    predicted -= 0.08f;
                    predicted *= 0.98f;

                    if (Math.abs(predicted) < 0.0005) predicted = 0;
                }

                if (!MathUtils.approxEquals(1E-5, predicted, move.getDeltaY())
                        && !getData().isLagging()
                        && predicted > 0
                        && getData().getBoundingBox()
                        .shrink(0, 0.1f, 0)
                        .grow(1,0,1)
                        .getCollidingBlockBoxes(getData().getPlayer()).size() == 0) {
                    if(vl++ > 9) {
                        flag("predicted=" + predicted + " deltaY=" + move.getDeltaY() + " vl=" + vl,
                                true, true, vl > 14 ? AlertTier.HIGH : AlertTier.LIKELY);
                    }
                    debug(Color.Green + "Flag: " + vl + " predicted=" + predicted + " deltaY="
                            + move.getDeltaY() + " vl=" + vl);
                } else {
                    vl-= vl > 0 ? 0.5 : 0;
                    debug("predicted=" + predicted + " deltaY=" + move.getDeltaY() + " vl=" + vl);
                }
            }
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
