package cc.funkemunky.anticheat.impl.checks.combat.autoclicker;

import cc.funkemunky.anticheat.api.checks.*;
import cc.funkemunky.anticheat.api.utils.MathUtils;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import lombok.val;
import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.List;

@Init
@Packets(packets = {Packet.Client.ARM_ANIMATION})
@CheckInfo(name = "Autoclicker (Type J)", description = "Checks if your CPS is constant nibba.", type = CheckType.AUTOCLICKER, cancelType = CancelType.INTERACT, maxVL = 50)
public class AutoclickerJ extends Check {

    private List<Long> cpsList = new ArrayList<>();
    private long lastClick;
    private double lastAverage, vl;
    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(MiscUtils.shouldReturnArmAnimation(getData())) {
            cpsList.clear();
            return;
        }

        val delta = timeStamp - lastClick;

        if(cpsList.size() > 15) {
            double average = 1000 / cpsList.stream().mapToLong(val -> val).average().getAsDouble();

            if(average > 7.4 && MathUtils.getDelta(average, lastAverage) < 0.75) {
                if(vl++ > 5) {
                    flag("your mom a hoe", true, true, vl > 10 ? AlertTier.HIGH : AlertTier.LIKELY);
                }
            } else vl-= vl > 0 ? 1 : 0;

            debug("avg=" + average + " vl=" + vl);
            lastAverage = average;
            cpsList.clear();
        } else cpsList.add(delta);

        lastClick = timeStamp;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
