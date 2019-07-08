package cc.funkemunky.anticheat.impl.checks.combat.autoclicker;

import cc.funkemunky.anticheat.api.checks.*;
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
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "Autoclicker (Type E)", description = "A normal click consistency check.", type = CheckType.AUTOCLICKER, cancelType = CancelType.INTERACT, maxVL = 50, executable = false)
public class AutoclickerE extends Check {

    private long lastTS;
    private double lastAverage;
    private int vl;
    private List<Long> msList = new ArrayList<>();

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(MiscUtils.shouldReturnArmAnimation(getData())) {
            lastTS = timeStamp;
            msList.clear();
            return;
        }

        long delta = timeStamp - lastTS;

        if(msList.size() >= 20) {
            double average = msList.stream().mapToDouble(lon -> lon).average().getAsDouble();

            if(MathUtils.getDelta(average, lastAverage) < 3 && average < 180) {
                if((vl = Math.min(12, vl + 1)) > 4) {
                    flag(average + "=" + lastAverage + ", vl=" + vl, true, true, vl > 8 ? AlertTier.HIGH : AlertTier.LIKELY);
                }
            } else vl-= vl > 0 ? 1 : 0;

            lastAverage = average;
            msList.clear();
        } else msList.add(delta);

        lastTS = timeStamp;
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
