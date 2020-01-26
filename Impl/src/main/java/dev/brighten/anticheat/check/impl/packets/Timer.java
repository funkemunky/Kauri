package dev.brighten.anticheat.check.impl.packets;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import lombok.val;

@CheckInfo(name = "Timer", description = "Checks the rate of packets coming in.",
        checkType = CheckType.BADPACKETS, punishVL = 175)
@Cancellable
public class Timer extends Check {

    private long lastTS, lRange;
    private EvictingList<Long> times = new EvictingList<>(30);

    @Packet
    public void onPacket(WrappedInFlyingPacket packet, long timeStamp) {
        long elapsed = timeStamp - lastTS;

        if(timeStamp - data.creation > 500 && !data.playerInfo.serverPos
                && timeStamp - data.playerInfo.lastServerPos > 60L) {
            times.add(elapsed);
            val summary = times.stream().mapToLong(val -> val).summaryStatistics();
            double average = summary.getAverage();
            double ratio = 50 / average;
            long range = summary.getMax() - summary.getMin();
            double pct = ratio * 100;

            if((pct > 100.2D) && (timeStamp - data.playerInfo.lastServerPos > 150L)
                    && MathUtils.getDelta(data.lagInfo.lastTransPing, data.lagInfo.transPing) < 30
                    && Kauri.INSTANCE.lastTickLag.hasPassed(5)
                    && (range < 200 || MathUtils.getDelta(range, lRange) < 75)
                    && Kauri.INSTANCE.tps > 18.5) {
                //Maybe lower threshold? I do not think it needs that high of one.
                if(vl++ > 45) flag("pct=" + MathUtils.round(pct, 2) + "%");
            } else vl-= vl > 0 ? 1.5 : 0;

            debug("pct=" + pct + ", vl=" + vl + ", elapsed=" + elapsed + "ms, avg=" + average + ", range=" + range);
            lRange = range;
        }
        lastTS = timeStamp;
    }
}