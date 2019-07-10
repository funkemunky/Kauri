package cc.funkemunky.anticheat.impl.checks.combat.autoclicker;

import cc.funkemunky.anticheat.api.checks.*;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import lombok.val;
import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.List;

@CheckInfo(name = "Autoclicker (Type G)", description = "Checks if the autoclicker clicks in a specific range set.", type = CheckType.AUTOCLICKER, cancelType = CancelType.INTERACT)
@Init
@Packets(packets = {Packet.Client.ARM_ANIMATION})
public class AutoclickerG extends Check {

    private double vl, vl2;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val swing = getData().getSwingProcessor();
        if(MiscUtils.shouldReturnArmAnimation(getData()) || (timeStamp - swing.getLastDequeueProcess()) > 5) return;

        if(swing.getStdDelta() < 5 && swing.getAverage() > 65 && swing.getAverage() < 120) {
            if(swing.getAverageDelta() > 5) {
                if(vl++ > 5) {
                    flag("avg=" + swing.getAverage() + " std=" + swing.getStd() + "% delta=" + swing.getStdDelta(), true, true, AlertTier.CERTAIN);
                } else if(vl > 2) {
                    flag("avg=" + swing.getAverage() + " std=" + swing.getStd() + "% delta=" + swing.getStdDelta(), true, true, AlertTier.HIGH);
                }
            } else if(vl2++ > 2) {
                flag("avg=" + swing.getAverage() + " std=" + swing.getStd() + "% delta=" + swing.getStdDelta(), true, false, AlertTier.POSSIBLE);
            }
            getData().getTypeH().flag(10, 8000L);
        } else {
            vl-= vl > 0 ? .5 : 0;
            vl2-= vl2 > 0 ? .5 : 0;
        }
        debug("avg=" + swing.getAverage() + " std=" + swing.getStd() + " delta=" + swing.getStdDelta() + " vl1=" + vl + " vl2=" + vl2);

    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
