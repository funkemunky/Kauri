package dev.brighten.anticheat.check.impl.packets;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.CheckType;
import dev.brighten.anticheat.check.api.Packet;

@CheckInfo(name = "Timer", description = "Checks the rate of packets coming in.",
        checkType = CheckType.BADPACKETS, punishVL = 175)
public class Timer extends Check {

    private long lastTS;
    private EvictingList<Long> times = new EvictingList<>(30);

    @Packet
    public void onPacket(WrappedInFlyingPacket packet, long timeStamp) {
        long elapsed = timeStamp - lastTS;

        if(timeStamp - data.creation > 500 && !data.playerInfo.serverPos) {
            if(elapsed > 4) times.add(elapsed);

            double average = times.stream().mapToLong(val -> val).average().orElse(50.0);
            double ratio = 50 / average;
            double pct = ratio * 100;

            if((pct > 101) && (timeStamp - data.playerInfo.lastServerPos > 150L)
                    && MathUtils.getDelta(data.lagInfo.lastTransPing, data.lagInfo.transPing) < 30
                    && Kauri.INSTANCE.lastTickLag.hasPassed(5)
                    && Kauri.INSTANCE.tps > 18.5) {
                if(vl++ > 80) flag("pct=" + MathUtils.round(pct, 2) + "%");
            } else vl-= vl > 0 ? 1.5 : 0;

            debug("pct=" + pct + ", vl=" + vl);
        }
        lastTS = timeStamp;
    }
}
