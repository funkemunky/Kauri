package cc.funkemunky.anticheat.impl.checks.combat.autoclicker;

import cc.funkemunky.anticheat.api.checks.*;
import cc.funkemunky.anticheat.api.utils.Interval;
import cc.funkemunky.anticheat.api.utils.MathUtils;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import lombok.val;
import org.bukkit.event.Event;

@Init
@CheckInfo(name = "Autoclicker (Type B)", description = "Checks for consistency within certain areas.", type = CheckType.AUTOCLICKER, cancelType = CancelType.INTERACT, maxVL = 50)
@Packets(packets = {Packet.Client.ARM_ANIMATION})
public class AutoclickerB extends Check {

    private Interval<Double> cpsList = new Interval<>(0, 30);
    private long lastClick;
    private double lastStd, lastAverage;
    private double vl;
    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(!MiscUtils.shouldReturnArmAnimation(getData())) {

            double cps = 1000f / (timeStamp - lastClick);

            if(cpsList.size() >= 30) {
                val std = (float) cpsList.std();
                val average = (float) cpsList.average();
                val distinct = cpsList.distinctCount();
                if(std > 3 && average > 8 && MathUtils.getDelta(std, lastStd) < 1.1 && MathUtils.getDelta(average, lastAverage) > 0.8) {
                    debug(Color.Green + "Flag");
                    vl+= 2;
                } else if(std < 2.5 && average > 8 && distinct > 17) {
                    debug(Color.Green + "Flag 2");
                    vl++;
                } else vl-= vl > 0 ? 2 : 0;

                if(vl > 6) {
                    flag("std=" + std + " avg=" + average + " distinct=" + distinct + " vl=" + vl, true, true, AlertTier.HIGH);
                }

                debug("std=" + std + " avg=" + average + " distinct=" + distinct + " vl=" + vl);
                lastStd = std;
                lastAverage = average;
                cpsList.clearIfMax();
                cpsList.clear();
            } else if(timeStamp - lastClick > 0) {
                cpsList.add(cps);
            }

            lastClick = timeStamp;
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
