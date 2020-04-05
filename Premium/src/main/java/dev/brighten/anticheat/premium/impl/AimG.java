package dev.brighten.anticheat.premium.impl;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.AtomicDouble;
import dev.brighten.anticheat.utils.Verbose;
import dev.brighten.api.check.CheckType;
import lombok.val;

import java.util.LongSummaryStatistics;

@CheckInfo(name = "Aim (G)", description = "A simple check to detect Vape's aimassist.",
        checkType = CheckType.AIM, developer = true, enabled = false, punishVL = 30)
public class AimG extends Check {

    private Verbose verbose = new Verbose(50, 6);
    private EvictingList<Long> ldeltaX = new EvictingList<>(25), ldeltaY = new EvictingList<>(25);

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(!packet.isLook() || data.playerInfo.cinematicMode) return;

        val deltaX = Math.abs(data.moveProcessor.deltaX - data.moveProcessor.lastDeltaX);
        val deltaY = Math.abs(data.moveProcessor.deltaY - data.moveProcessor.lastDeltaY);

        ldeltaX.add(deltaX);
        ldeltaY.add(deltaY);

        if(ldeltaY.size() >= 20 || ldeltaX.size() >= 20) {
            LongSummaryStatistics xsummary = ldeltaX.parallelStream().mapToLong(l -> l).summaryStatistics();

            LongSummaryStatistics distinctX = ldeltaX.parallelStream().mapToLong(l -> l).distinct().summaryStatistics(),
                    distinctY = ldeltaY.parallelStream().mapToLong(l -> l).distinct().summaryStatistics();

            AtomicDouble std = new AtomicDouble();

            ldeltaX.parallelStream().forEach(l -> std.addAndGet(Math.pow(l - distinctX.getAverage(), 2)));

            std.set(Math.sqrt(std.get() / ldeltaX.size()));

            if(distinctY.getCount() <= 6 && distinctX.getCount() <= 13
                    && std.get() < 11 && data.moveProcessor.deltaX > 30) {
                vl++;
                flag("ydis=%v xdis=%v avg=%v.2 std=%v.2 deltaX=%v", distinctY.getCount(), distinctX.getCount(),
                        xsummary.getAverage(), std.get(), data.moveProcessor.deltaX);
            }
            debug("xdis=%v ydis=%v xavg=%v.2 xstd=%v.2 deltaX=%v vl=%v", distinctX.getCount(), distinctY.getCount(),
                    xsummary.getAverage(), std.get(), data.moveProcessor.deltaX, vl);
        }
    }
}
