package cc.funkemunky.anticheat.impl.checks.combat.autoclicker;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import lombok.val;
import org.bukkit.event.Event;

import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;

@Init
@Packets(packets = {Packet.Client.ARM_ANIMATION})
@CheckInfo(name = "Autoclicker (Type J)", description = "test", type = CheckType.AUTOCLICKER, cancelType = CancelType.INTERACT, developer = true)
public class AutoclickerJ extends Check {

    private Deque<Double> list = new LinkedList<>();
    private long lastTimeStamp;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(!MiscUtils.shouldReturnArmAnimation(getData())) {
            val delta = timeStamp - lastTimeStamp;
            val cps = 1000D / delta;


            if(list.size() >= 80) {
                val stddev = getStd(list);

                debug("STD: " + stddev);

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
        double mean = getMean(list);

        double total = 0;

        for (Double val : list) {
            total+= Math.pow(val - mean, 2);
        }

        return Math.sqrt(total / list.size());
    }
}
