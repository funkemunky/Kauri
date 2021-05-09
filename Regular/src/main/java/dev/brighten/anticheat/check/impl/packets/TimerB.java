package dev.brighten.anticheat.check.impl.packets;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutPositionPacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.SimpleAverage;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Timer (B)", description = "Checks the rate of packets coming in.",
        checkType = CheckType.BADPACKETS, vlToFlag = 20, developer = true)
@Cancellable
public class TimerB extends Check {

    private long lastFlying, totalTimer;
    private int buffer;
    private SimpleAverage averageIncrease = new SimpleAverage(5, 0);

    @Packet
    public void onTeleport(WrappedOutPositionPacket event) {
        totalTimer-= 50;
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long now) {
        long delta = now - lastFlying;

        long before = totalTimer;
        totalTimer+= 50;
        totalTimer-= lastFlying == 0 ? 50 : delta;

        long increase = totalTimer - before;

        averageIncrease.add(increase);

        double avgIncrease = averageIncrease.getAverage();

        if(totalTimer > 100) {
            vl++;
            flag("t=%s aInc=%.1f", totalTimer, avgIncrease);
            totalTimer = 0;
        }

        debug("delta=%sms total=%s inc=%s avgInc=%.1f", delta, totalTimer, increase, avgIncrease);

        lastFlying = now;
    }
}