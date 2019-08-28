package cc.funkemunky.anticheat.impl.checks.combat.autoclicker;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
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

    private Interval<Float> cpsList = new Interval<>(0, 40);
    private long lastClick;
    private double lastStd, lastAverage;
    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(!MiscUtils.shouldReturnArmAnimation(getData())) {
            if(timeStamp - lastClick > 2000) {
                cpsList.clear();
                return;
            }

            float cps = 1000f / timeStamp;

            if(cpsList.size() >= 40) {
                val std = cpsList.std();
                val average = cpsList.average();
                val distinct = cpsList.distinctCount();
                if(std > 4 && MathUtils.getDelta(std, lastStd) < 1) {
                    debug(Color.Green + "Flag");
                }
                if(MathUtils.getDelta(average, lastAverage) < 1 && std > 3) {
                    debug(Color.Green + "Flag 2");
                }
                if(distinct > 15 && MathUtils.getDelta(std, lastStd) < 1) {
                    debug(Color.Green + "Flag 3");
                }

                debug("std=" + std + " avg=" + average + " distinct=" + distinct);
                lastStd = std;
                lastAverage = average;
                cpsList.clear();
            } else cpsList.add(cps);

            lastClick = timeStamp;
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
