package dev.brighten.anticheat.check.impl.packets;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockPlacePacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutPositionPacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.SimpleAverage;
import dev.brighten.anticheat.utils.timer.Timer;
import dev.brighten.anticheat.utils.timer.impl.TickTimer;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Timer (B)", description = "Checks the rate of packets coming in.",
        checkType = CheckType.BADPACKETS, vlToFlag = 20, developer = true)
@Cancellable
public class TimerB extends Check {

    private long lastFlying;
    private double totalTimer = -1000;
    private int buffer;
    private final SimpleAverage averageIncrease = new SimpleAverage(5, 0);
    private final Timer lastFlag = new TickTimer();

    @Packet
    public void onTeleport(WrappedOutPositionPacket event) {
        totalTimer-= 50;
    }

    @Packet
    public void onBlockPlace(WrappedInBlockPlacePacket packet) {
        totalTimer-= 50;
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long now) {
        long delta = now - lastFlying;

        double before = totalTimer;
        totalTimer+= 49.5;
        totalTimer-= lastFlying == 0 ? 50 : delta;

        double increase = totalTimer - before;

        averageIncrease.add(increase);

        double avgIncrease = averageIncrease.getAverage();

        totalTimer = Math.max(totalTimer, -1000);

        if(totalTimer > 150) {
            if(++buffer > 3) {
                vl++;
                flag("t=%.1f aInc=%.1f", totalTimer, avgIncrease);
            }
            lastFlag.reset();
            totalTimer = 0;
        } else if(lastFlag.isPassed(120)) {
            buffer = 0;
        }

        debug("delta=%sms total=%s inc=%s avgInc=%.1f", delta, totalTimer, increase, avgIncrease);

        lastFlying = now;
    }
}