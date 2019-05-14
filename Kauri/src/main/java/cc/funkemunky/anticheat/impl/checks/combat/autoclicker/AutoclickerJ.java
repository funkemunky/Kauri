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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

//@Init
@CheckInfo(name = "Autoclicker (Type J)", description = "Looks for autoclickers with repeating time differences.", type = CheckType.AUTOCLICKER, cancelType = CancelType.INTERACT)
@Packets(packets = {Packet.Client.ARM_ANIMATION})
public class AutoclickerJ extends Check {

    private List<Double> list = new ArrayList<>();
    private long lastTimeStamp;
    private double lastRange;
    private int vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(MiscUtils.shouldReturnArmAnimation(getData())) return;

        double cps = 1000D / (timeStamp - lastTimeStamp);

        if(list.size() >= 40) {
            list.sort(Comparator.reverseOrder());

            val range = list.get(0) - list.get(list.size() - 1);
            val delta = MathUtils.getDelta(range, lastRange);

            if(delta < 0.5) {
                if(vl++ > 5) {
                    flag(delta + "<-0.1", true, true);
                }
            } else vl-= vl > 0 ? 2 : 0;

            debug("range=" + range + " delta=" + delta + " vl=" + vl);
            lastRange = range;
            list.clear();
        } else if(cps > 0 && cps < 20) {
            list.add(cps);
        }

        lastTimeStamp = timeStamp;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
