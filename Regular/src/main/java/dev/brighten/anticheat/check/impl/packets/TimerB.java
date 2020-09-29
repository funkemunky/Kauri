package dev.brighten.anticheat.check.impl.packets;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@CheckInfo(name = "Timer (B)", description = "Checks the rate of packets coming in.",
        checkType = CheckType.BADPACKETS, vlToFlag = 20, developer = true)
@Cancellable
public class TimerB extends Check {

    private int buffer, lagTicks;
    private final List<Long> list = new EvictingList<>(20);
    private long lastFlying = System.currentTimeMillis();

    @Packet
    public void onPacket(WrappedInFlyingPacket packet, long current) {
        long delta = current - lastFlying;

        /*if(list.size() > 20 && (delta > 5 && delta < 90)) {
            if(list.size() > 21) {
                list.stream().filter(l -> l < 5 || l > 90).forEach(list::remove);
            }
            for (int i = 0; i < list.size() - 20; i++) {
                list.remove(0);
            }
        }*/

        int ticks = Math.round(delta / 50.f) - 1;

        if(ticks > 0) this.lagTicks+= ticks;

        if((lagTicks-= lagTicks > 0 ? 1 : 0) <= 0)
        list.add(delta);

        double average = list.stream().mapToLong(l -> l).average().orElse(50);

        double pct = 50 / average * 100;

        if(pct > 101 && list.size() > 15) {
            if(++buffer > 20) {
                vl++;
                flag("pct=%v.1", pct);
            }
        } else if(buffer > 0) buffer-= 1f;

        debug("delta=%v.2 lagTicks=%v", average, lagTicks);
        lastFlying = current;
    }
}