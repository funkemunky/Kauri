package dev.brighten.anticheat.check.impl.packets;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.Tuple;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.MiscUtils;
import dev.brighten.api.check.CheckType;
import lombok.val;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

@CheckInfo(name = "Timer (B)", description = "Checks the rate of packets coming in.",
        checkType = CheckType.BADPACKETS, vlToFlag = 20, developer = true)
@Cancellable
public class TimerB extends Check {

    private int buffer;
    private final List<Long> list = Collections.synchronizedList(new ArrayList<>());
    private long lastFlying = System.currentTimeMillis();

    @Packet
    public void onPacket(WrappedInFlyingPacket packet, long current) {
        long delta = current - lastFlying;

        if(list.size() > 40) {
            //Removing all values until its 30 or less

            Tuple<List<Long>, List<Long>> indexesToRemove = MiscUtils.getOutliersLong(list);

            for (Long aLong : indexesToRemove.one) {
                synchronized (list) {
                    list.remove(aLong);
                }
            }

            indexesToRemove = null;

            while (list.size() > 40) {
                synchronized (list) {
                    list.remove(0);
                }
            }
        }

        check: {

            if(data.playerInfo.lastTeleportTimer.isPassed(2)
                    && data.playerInfo.lastRespawnTimer.isPassed(10)) {
                synchronized (list) {
                    list.add(delta);
                }
            } else break check;

            double average = list.stream().mapToLong(l -> l).average().orElse(50);

            double pct = 50 / average * 100;

            if(pct > 100.8 && list.size() > 30) {
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