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
import dev.brighten.anticheat.utils.SimpleAverage;
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
    private SimpleAverage averageLong = new SimpleAverage(30, 50);

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long now) {
        long delta = now - lastFlying;

        if(data.playerInfo.lastTeleportTimer.isPassed(1) && !data.playerInfo.doingTeleport) {
            if(delta < 3000L)
                averageLong.add(delta);

            double average = averageLong.getAverage(), ratio = 50. / average;

            if(ratio > 1.03) {
                if(++buffer > 75) {
                    vl++;
                    flag("r=%.2f b=%s", ratio, buffer);
                }
            } else buffer = 0;

            debug("[%s] ratio=%.3f avg=%.2f", buffer, ratio, average);
        }

        lastFlying = now;
    }
}