package cc.funkemunky.anticheat.impl.checks.combat.autoclicker;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
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
    private double lastStd, vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(MiscUtils.shouldReturnArmAnimation(getData())) {
            debug("breaking block");
            return;
        }

        long ms = timeStamp - lastTimeStamp;

        if(list.size() >= 20) {
            double average = list.stream().mapToLong(val -> val).average().orElse(0);
            double std = Math.sqrt(list.stream().mapToDouble(val -> Math.pow(val - average, 2)).average().orElse(0));
            double avgDelta = Math.abs(std - lastStd);

            if(avgDelta < 5 && average > 50 && average < 120 && std < 70) {
                if(vl++ > 7) {
                    banUser();
                } else if(vl > 4) {
                    flag("delta=" + avgDelta + " std=" + std, true, true);
                }
            } else vl-= vl > 0 ? 1 : 0;

            debug("avg=" + average + " std=" + std);
            list.clear();
            lastStd = std;
        } else if(ms > 0 && ms < 400) {
            list.add(ms);
        }
        lastTimeStamp = timeStamp;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
