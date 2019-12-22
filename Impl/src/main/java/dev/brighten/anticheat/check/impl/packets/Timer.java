package dev.brighten.anticheat.check.impl.packets;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.objects.Interval;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import lombok.val;

import java.util.List;
import java.util.stream.Collectors;

@CheckInfo(name = "Timer", description = "Checks the rate of packets coming in.",
        checkType = CheckType.BADPACKETS, punishVL = 175)
public class Timer extends Check {

    private long lastTS, lastElapsed;
    private int ticks;
    private Interval times = new Interval(30);

    @Packet
    public void onPacket(WrappedInFlyingPacket packet, long timeStamp) {
        long elapsed = timeStamp - lastTS;
        long deltaElapsed = Math.round((elapsed + lastElapsed) / 2D);

        if(timeStamp - data.creation > 500 && !data.playerInfo.serverPos && (ticks > 6 || ticks++ > 4)) {
            times.add(deltaElapsed);

            double std = times.std();
            double taverage = times.average();
            val dev = times.stream().filter(v -> MathUtils.getDelta(taverage, v) < std).collect(Collectors.toList());
            double average = times.size() - dev.size() > 10 ? taverage : dev.stream().mapToDouble(v -> v).average().orElse(50);
            double ratio = 50 / average;
            double pct = ratio * 100;

            if((pct > 100.1D) && (timeStamp - data.playerInfo.lastServerPos > 150L)
                    && MathUtils.getDelta(data.lagInfo.lastTransPing, data.lagInfo.transPing) < 30
                    && Kauri.INSTANCE.lastTickLag.hasPassed(5)
                    && Kauri.INSTANCE.tps > 18.5) {
                //Maybe lower threshold? I do not think it needs that high of one.
                if(vl++ > 80) flag("pct=" + MathUtils.round(pct, 2) + "%");
            } else vl-= vl > 0 ? 1.5 : 0;

            debug("pct=" + pct + ", vl=" + vl+ " Delapsed=" + deltaElapsed + " elapsed=" + elapsed);
        }
        lastTS = timeStamp;
        lastElapsed = elapsed;
    }
}
