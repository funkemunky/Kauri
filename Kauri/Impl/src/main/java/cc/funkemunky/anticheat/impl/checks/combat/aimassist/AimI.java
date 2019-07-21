package cc.funkemunky.anticheat.impl.checks.combat.aimassist;

import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Interval;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {Packet.Client.LOOK, Packet.Client.POSITION_LOOK})
//@Init
@CheckInfo(name = "Aim (Type I)", developer = true, type = CheckType.AIM)
public class AimI extends Check {

    private double lastMD;

    private Interval<Double> interval = new Interval<>(0, 20);

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        float f = 0.6F + 0.2F;
        float f1 = f * f * f * 8.0F;
        double mouseDelta = move.getYawDelta() / f1 / .15;

        interval.add(mouseDelta);
        double avg = interval.average(), range = (interval.max() - interval.min());
        debug("avg=" + interval.average() + " range=" + (interval.max() - interval.min()));
        interval.clearIfMax();
        //debug("pred=" + mouseDelta);
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
