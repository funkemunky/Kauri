package dev.brighten.anticheat.premium.impl;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.objects.Interval;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.Verbose;
import dev.brighten.api.check.CheckType;
import lombok.val;

import java.util.Deque;
import java.util.DoubleSummaryStatistics;
import java.util.LinkedList;

@CheckInfo(name = "Aim (G)", description = "A simple check to detect Vape's aimassist.",
        checkType = CheckType.AIM, developer = true, enabled = false, punishVL = 30)
public class AimG extends Check {

    private Verbose verbose = new Verbose(50, 6);
    private Interval ldeltaX = new Interval(50);
    private Interval ldeltaY = new Interval(50);
    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(!packet.isLook() || data.playerInfo.cinematicMode) return;

        val deltaX = Math.abs(data.moveProcessor.deltaX - data.moveProcessor.lastDeltaX);
        val deltaY = Math.abs(data.moveProcessor.deltaY - data.moveProcessor.lastDeltaY);

        ldeltaX.add(data.moveProcessor.deltaX);
        ldeltaY.add(deltaY);

        if(ldeltaY.size() >= 15 ||  ldeltaX.size() >= 15) {
            long xdis = ldeltaX.distinctCount(), ydis = ldeltaY.distinctCount();
            DoubleSummaryStatistics summary = ldeltaX.getSummary();

            if(ydis <= 3 && xdis < 12 && summary.getAverage() > 22) {
                vl++;
                flag("ydis=%v xdis=%v", ydis, xdis);
            }
            debug("xdis=%v ydis=%v xavg=%v.2 xstd=%v.2", xdis, ydis, summary.getAverage(), ldeltaX.std());

            ldeltaX.clear();
            ldeltaY.clear();
        }
    }
}
