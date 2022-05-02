package dev.brighten.anticheat.check.impl.packet.timer;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.SimpleAverage;
import dev.brighten.api.check.CheckType;

/*
 * This check is designed to be impervious to lag spikes and concurrency problems. For every flying packet coming in
 * we add 50ms to our total time, starting when they initially start sending PacketPlayInFlying. We set our threshold
 * to the current time with the highest millisecond average (usually above 50) they had created. This is to prevent
 * sudden concurrency problems from lag spikes causing a false positive. If a player starts adding time above this
 * threshold, then their game must be sending packets faster than possible and therefore we can conclude they are
 * using Timer.
 */
@CheckInfo(name = "Timer (A)", description = "Checks the rate of packets coming in.",
        checkType = CheckType.BADPACKETS, vlToFlag = 2, punishVL = 10)
@Cancellable
public class TimerA extends Check {

    private long lastFlying;
    private int buffer;
    private SimpleAverage simpleAvg = new SimpleAverage(20, 50);

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long now) {
        if(data.playerInfo.doingTeleport || data.playerInfo.lastTeleportTimer.isNotPassed(1)) return;

        long delta = now - lastFlying;

        //Adding to our total average
        simpleAvg.add(delta);

        double averageInterval = simpleAvg.getAverage(), pct = 50 / averageInterval * 100;

        if(pct > 112) {
            if(data.lagInfo.lastPacketDrop.isPassed(5) && ++buffer > 45) {
                vl++;
                flag("pct=%.1f%% avg=%.1f b=%s", pct, averageInterval, buffer);
            }
        } else if(buffer > 0) buffer-= 8;

        debug("b=%s int=%s avg=%.1f pct=%.1f%%", buffer, delta, averageInterval, pct);

        lastFlying = now;
    }
}