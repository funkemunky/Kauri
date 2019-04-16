package cc.funkemunky.anticheat.impl.checks.combat.autoclicker;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MathUtils;
import lombok.val;
import org.bukkit.event.Event;

import java.util.*;
import java.util.stream.Collectors;

@Init
@Packets(packets = {Packet.Client.ARM_ANIMATION})
@CheckInfo(name = "Autoclicker (Type I)", description = "test", type = CheckType.AUTOCLICKER, cancelType = CancelType.INTERACT)
public class AutoclickerI extends Check {

    private List<Double> list = new ArrayList<>();
    private long lastTimeStamp;
    private double lastStd, vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(!MiscUtils.shouldReturnArmAnimation(getData())) {
            val delta = timeStamp - lastTimeStamp;
            val cps = 1000D / delta;


            if(list.size() >= 40) {
                val mean = getMean(list);
                val stddev = getStd(list, mean);
                val count = checkStd(list, mean, stddev);

                list.sort(Comparator.reverseOrder());

                val range = Math.abs(list.get(0) - list.get(list.size() - 1));
                val pct = MathUtils.round(count / (double) list.size() * 100, 3);
                val stdDelta = MathUtils.getDelta(stddev, lastStd);

                if(pct < 75 && stdDelta < 0.5) {
                    if(vl++ > 6) {
                        flag("pct=" + pct + "%, std=" + stdDelta, true, true);
                    }
                } else vl -= vl > 0 ? 0.5 : 0;

                val pctSTD =  MathUtils.round((1 - (stdDelta / mean)) * 100, 3);

                debug("vl: " + vl + "STD: " + stddev + " count: " + count + " range: " + range + " pct: " + pct + " pctstd: " + pctSTD);

                lastStd = stddev;
                list.clear();
            } else if(cps < 20 && cps > 0) {
                list.add(cps);
            }

            lastTimeStamp = timeStamp;
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }

    private double getMean(Collection<Double> list) {
        double total = 0;

        for (Double val : list) {
            total+= val;
        }

        return total / list.size();
    }

    private double getStd(Collection<Double> list) {
        return getStd(list, getMean(list));
    }

    private double getStd(Collection<Double> list, double mean) {
        double total = 0;

        for (Double val : list) {
            total+= Math.pow(val - mean, 2);
        }

        return Math.sqrt(total / list.size());
    }

    private long checkStd(Collection<Double> list, double mean, double std) {
        return list.stream().filter(val -> val > mean - std && val < mean + std).count();
    }

    private long checkStd(Collection<Double> list, double mean) {
        val std = getStd(list, mean);

        return checkStd(list, mean, std);
    }

    private long checkStd(Collection<Double> list) {
        val mean = getMean(list);
        val std = getStd(list, mean);

        return checkStd(list, mean, std);
    }
}
