package dev.brighten.anticheat.check.impl.combat.autoclicker;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.utils.math.cond.MaxInteger;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.anticheat.utils.GraphUtil;
import dev.brighten.api.check.CheckType;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

@CheckInfo(name = "Autoclicker (F)", description = "Checks for consistency through graphical means (Elevated).",
        checkType = CheckType.AUTOCLICKER, punishVL = 50, developer = true)
@Cancellable(cancelType = CancelType.INTERACT)
public class AutoclickerF extends Check {

    private MaxInteger verbose = new MaxInteger(100);

    private final EvictingList<Long> delays = new EvictingList<>(10);
    private final Deque<Integer> ratioDeque = new LinkedList<>();

    private long lastTime;

    @Packet
    public void onClick(WrappedInArmAnimationPacket packet, long timeStamp) {
        if(data.playerInfo.breakingBlock
                || data.playerInfo.lastBrokenBlock.hasNotPassed(5)
                || data.playerInfo.lastBlockPlace.hasNotPassed(4)) {
            lastTime = timeStamp;
            return;
        }
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
                double avg = 1000D / delays.stream().mapToLong(v -> v).average().orElse(0);

                AtomicInteger level = new AtomicInteger();
                ratioDeque.stream().filter(i -> i == 0 || i == 1)
                        .forEach(i -> level.incrementAndGet());

                if (level.get() == 50 && avg > 7) {
                    verbose.add(3);

                    if (verbose.value() >= 10) {
                        vl++;
                        flag("lvl=%1 avg=%2", level.get(), avg);
                    }
                } else verbose.subtract(2);

                debug("size=%1 avg=%2 verbose=%3", level.get(), avg, verbose.value());
                ratioDeque.clear();
            }
            debug("ratio=" + ratio);
        }
        this.lastTime = timeStamp;
    }
}
