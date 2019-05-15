package cc.funkemunky.anticheat.impl.checks.combat.autoclicker;

import cc.funkemunky.anticheat.api.checks.*;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.List;

@CheckInfo(name = "Autoclicker (Type K)", description = "test", type = CheckType.AUTOCLICKER, cancelType = CancelType.INTERACT)
@Init
@Packets(packets = {Packet.Client.ARM_ANIMATION})
public class AutoclickerK extends Check {

    private long lastTimeStamp;
    private List<Long> list = new ArrayList<>();
    private double lastStd, lastAverage, vl, vl2;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(MiscUtils.shouldReturnArmAnimation(getData())) return;

        long ms = timeStamp - lastTimeStamp;

        if(list.size() >= 20) {
            double average = list.stream().mapToLong(val -> val).average().orElse(0);
            double std = Math.sqrt(list.stream().mapToDouble(val -> Math.pow(val - average, 2)).average().orElse(0));
            double stdDelta = Math.abs(std - lastStd), avgDelta = Math.abs(average - lastAverage);

            if(stdDelta < 5 && average > 50 && average < 120) {
                if(avgDelta > 5) {
                    if(vl++ > 5) {
                        flag("avg=" + average + " std=" + std + "% delta=" + stdDelta, true, true, AlertTier.CERTAIN);
                    } else if(vl > 2) {
                        flag("avg=" + average + " std=" + std + "% delta=" + stdDelta, true, true, AlertTier.HIGH);
                    }
                } else if(vl2++ > 2) {
                    flag("avg=" + average + " std=" + std + "% delta=" + stdDelta, true, false, AlertTier.POSSIBLE);
                }
            } else {
                vl-= vl > 0 ? .5 : 0;
                vl2-= vl2 > 0 ? .5 : 0;
            }

            debug("avg=" + average + " std=" + std + "vl=" + vl);
            list.clear();
            lastStd = std;
            lastAverage = average;
        } else if(ms > 0 && ms < 400) {
            list.add(ms);
        }
        lastTimeStamp = timeStamp;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
