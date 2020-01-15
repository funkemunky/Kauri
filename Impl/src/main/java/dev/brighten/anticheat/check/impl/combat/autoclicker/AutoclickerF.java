package dev.brighten.anticheat.check.impl.combat.autoclicker;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.EvictingList;
import dev.brighten.anticheat.utils.GraphUtil;
import dev.brighten.api.check.CheckType;
import lombok.val;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@CheckInfo(name = "Autoclicker (F)", description = "Autoclicker check detecting based on graphs",
        checkType = CheckType.AUTOCLICKER, punishVL = 10)
public class AutoclickerF extends Check {

    private int vl;

    private final EvictingList<Long> delays = new EvictingList<>(10);
    private final Deque<Integer> ratioDeque = new LinkedList<>();

    private long lastTime;

    @Packet
    public void onClick(WrappedInArmAnimationPacket packet, long timeStamp) {
        long delay = timeStamp - this.lastTime;

        if (delay > 0L && delay < 400L) {
            delays.add(delay);

            GraphUtil.GraphResult graph = GraphUtil.getGraphLong(delays);

            if (graph.getPositives() == 0) {
                return;
            }

            int ratio = graph.getNegatives() / graph.getPositives();

            this.ratioDeque.add(ratio);

            if (ratioDeque.size() == 50) {
                AtomicInteger level = new AtomicInteger();
                ratioDeque.stream().filter(i -> i == 0 || i == 1).forEach(i -> level.incrementAndGet());

                if (level.get() == 50) {
                    vl += 5;

                    if (vl >= 10) {
                        this.flag(level.get() + "lvl");
                    }
                } else {
                    vl = Math.max(vl - 2, 0);
                }

                ratioDeque.clear();
            }
        }
        this.lastTime = timeStamp;
    }
}
