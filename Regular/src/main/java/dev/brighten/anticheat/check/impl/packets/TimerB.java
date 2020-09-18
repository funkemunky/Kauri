package dev.brighten.anticheat.check.impl.packets;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInTransactionPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutPositionPacket;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.math.RollingAverage;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.ExponentialMovingAverage;
import dev.brighten.anticheat.utils.MiscUtils;
import dev.brighten.api.check.CheckType;
import lombok.val;

@CheckInfo(name = "Timer (B)", description = "Checks the rate of packets coming in.",
        checkType = CheckType.BADPACKETS, vlToFlag = 4, developer = true)
@Cancellable
public class TimerB extends Check {

    private int ticks, buffer;
    private ExponentialMovingAverage average = new ExponentialMovingAverage(50);
    private long lastFlying = System.currentTimeMillis();

    @Packet
    public void onPacket(WrappedInFlyingPacket packet, long current) {

        double avg = average.average(current - lastFlying);

        debug("std=%v.5", avg);
        lastFlying = current;
    }
}