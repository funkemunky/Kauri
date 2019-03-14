package cc.funkemunky.anticheat.impl.checks.combat.autoclicker;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.MathUtils;
import lombok.val;
import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Packets(packets = {Packet.Client.ARM_ANIMATION})
public class AutoclickerG extends Check {

    private long lastTimeStamp, lastRange;
    private double vl;
    private List<Long> times = new CopyOnWriteArrayList<>();

    public AutoclickerG(String name, String description, CheckType type, CancelType cancelType, int maxVL, boolean enabled, boolean executable, boolean cancellable) {
        super(name, description, type, cancelType, maxVL, enabled, executable, cancellable);
    }

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val elapsed = timeStamp - lastTimeStamp;

        if (elapsed > 2 && !MiscUtils.shouldReturnArmAnimation(getData())) {
            if (times.size() >= 20) {
                val range = getRange(times);
                val average = getAverageCPS(times);

                if (average > 9 && (range < 65 || MathUtils.getDelta(range, lastRange) < 3)) {
                    if (vl++ > 5)
                        flag(range + "<-65 || " + range + "≈" + lastRange + " [" + MathUtils.round(average, 2) + " CPS]", true, true);
                } else vl -= vl > 0 ? 0.5 : 0;

                debug("VL: " + vl + " RANGE: " + range + " AVERAGE: " + average);
                lastRange = range;
                times.clear();
            } else {
                times.add(elapsed);
            }
        }

        debug(getData().isBreakingBlock() + " , " + getData().getLastBlockPlace().getPassed());

        lastTimeStamp = timeStamp;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }

    private long getRange(List<Long> list) {
        List<Long> use = new ArrayList<>(list);
        Collections.sort(use);

        return MathUtils.getDelta(use.get(0), use.get(use.size() - 1));
    }

    private double getAverageCPS(List<Long> list) {
        List<Double> use = new ArrayList<>();

        list.forEach(value -> use.add(1000D / value));

        double total = 0;

        for (double value : use) {
            total += value;
        }

        total /= use.size();

        return total;
    }
}
