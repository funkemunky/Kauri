package cc.funkemunky.anticheat.impl.checks.combat.aimassist;

import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Interval;
import cc.funkemunky.anticheat.api.utils.MathUtils;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import lombok.val;
import org.bukkit.event.Event;

@Init
@CheckInfo(name = "Aim (Type J)", type = CheckType.AIM)
@Packets(packets = {Packet.Client.LOOK, Packet.Client.POSITION_LOOK})
public class AimJ extends Check {

    private Interval<Double> interval = new Interval<>(0, 20);
    private double lastYawDelta;
    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        double yawDelta = MathUtils.getDistanceBetweenAngles(move.getFrom().getYaw(), move.getTo().getYaw());

        double gcd = (double) MiscUtils.gcd((long) yawDelta * move.getOffset(), (long) lastYawDelta * move.getOffset());

        if(interval.size() >= 20) {
            double first = interval.getFirst() / move.getOffset(), last = interval.getLast() / move.getOffset();
            double avg = interval.average() / move.getOffset();
            double std = interval.std() / move.getOffset();
            double delta = Math.abs(first - last), range = (interval.max() / move.getOffset()) - (interval.min() / move.getOffset());
            if(delta == range) {
                debug(Color.Green + " Flag");
            }
            debug("distinct=" + interval.distinctCount() + " avg=" + avg + " range=" + range + " delta=" + delta + " std=" + std);
            interval.clear();
        }

        interval.add(gcd);
        lastYawDelta = yawDelta;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
