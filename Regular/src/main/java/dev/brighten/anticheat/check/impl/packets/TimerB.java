package dev.brighten.anticheat.check.impl.packets;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

@CheckInfo(name = "Timer (B)", description = "Checks the rate of packets coming in.",
        checkType = CheckType.BADPACKETS, vlToFlag = 20, developer = true)
@Cancellable
public class TimerB extends Check {

    private int buffer;
    private boolean clearing;
    private final List<Long> list = Collections.synchronizedList(new ArrayList<>());
    private long lastFlying = System.currentTimeMillis();

    @Packet
    public void onPacket(WrappedInFlyingPacket packet, long current) {
        long delta = current - lastFlying;

        if(list.size() > 30 && (list.size() > 60 || (delta > 5 && delta < 90)) && !clearing) {
            if(!clearing && list.size() > 31) {
                clearing = true;
                synchronized (list) {
                    list.stream().filter(l -> l < 5 || l > 90)
                            .forEach(list::remove);
                }
                clearing = false;
            }
            //Removing all values until its 30 or less
            if(!clearing) {
                clearing = true;
                while (list.size() > 40) {
                    synchronized (list) {
                        list.remove(0);
                    }
                }
                clearing = false;
            }
            clearing = false;
        }

        check: {
            if(clearing) break check;

            if(!data.playerInfo.doingTeleport
                    && !data.playerInfo.serverPos
                    && data.playerInfo.lastRespawnTimer.isPassed(10))
                list.add(delta);

            if(clearing) break check;
            double average = list.stream().mapToLong(l -> l).average().orElse(50);

            double pct = 50 / average * 100;

            if(pct > 101 && list.size() > 30) {
                if(++buffer > 30) {
                    vl++;
                    flag("pct=%v.1", pct);
                }
            } else if(buffer > 0) buffer-= 2f;

            debug("delta=%v.2", average);
        }
        lastFlying = current;
    }
}