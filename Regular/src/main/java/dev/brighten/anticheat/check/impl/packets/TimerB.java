package dev.brighten.anticheat.check.impl.packets;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.Tuple;
import cc.funkemunky.api.utils.math.RollingAverage;
import cc.funkemunky.api.utils.math.RollingAverageDouble;
import cc.funkemunky.api.utils.math.RollingAverageLong;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.MiscUtils;
import dev.brighten.api.check.CheckType;
import lombok.val;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

@CheckInfo(name = "Timer (B)", description = "Checks the rate of packets coming in.",
        checkType = CheckType.BADPACKETS, vlToFlag = 20, developer = true)
@Cancellable
public class TimerB extends Check {

    private long lastFlying;
    private int buffer;
    private RollingAverage averageLong = new RollingAverage(20);
    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long millis) {
        long current = System.nanoTime();
        long delta = current - lastFlying;
        double milliSeconds = delta / 1E6D;

        averageLong.add(milliSeconds, millis);

        double average = averageLong.getAverage(), ratio = 50. / average;

        if(ratio > 1.01) {
            if(++buffer > 60) {
                vl++;
                flag("r=%v.1 b=%v", ratio, buffer);
            }
        } else buffer = 0;

        debug("[%v] ratio=%v.3 avg=%v.2", buffer, ratio, average);

        if(!data.playerInfo.serverPos)
        lastFlying = current;
    }
}