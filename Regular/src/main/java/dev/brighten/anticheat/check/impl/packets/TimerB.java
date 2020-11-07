package dev.brighten.anticheat.check.impl.packets;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@CheckInfo(name = "Timer (B)", description = "Checks the rate of packets coming in.",
        checkType = CheckType.BADPACKETS, vlToFlag = 20, developer = true)
@Cancellable
public class TimerB extends Check {

    private int buffer;
    private final Deque<Long> list = new LinkedList<>();
    private long lastFlying = System.currentTimeMillis();

    @Packet
    public void onPacket(WrappedInFlyingPacket packet, long current) {
        long delta = current - lastFlying;

        if(list.size() > 30 && (list.size() > 60 || (delta > 5 && delta < 90))) {
            if(list.size() > 31) {
                list.stream().filter(l -> l < 5 || l > 90).forEach(list::remove);
            }
            //Removing all values until its 30 or less
            while(list.size() > 40) {
                list.removeFirst();
            }
        }

        list.add(delta);

        double average = list.stream().mapToLong(l -> l).average().orElse(50);

        double pct = 50 / average * 100;

        if(pct > 101 && list.size() > 30) {
            if(++buffer > 20) {
                vl++;
                flag("pct=%v.1", pct);
            }
        } else if(buffer > 0) buffer-= 1f;

        debug("delta=%v.2", average);
        lastFlying = current;
    }
}