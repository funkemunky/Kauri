package cc.funkemunky.anticheat.impl.checks.combat.autoclicker;

import cc.funkemunky.anticheat.api.checks.*;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {Packet.Client.ARM_ANIMATION})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "Autoclicker (Type E)", description = "A normal click consistency check.", type = CheckType.AUTOCLICKER, cancelType = CancelType.INTERACT, maxVL = 50, executable = false)
public class AutoclickerE extends Check {

    private int vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val swing = getData().getSwingProcessor();
        if(!MiscUtils.shouldReturnArmAnimation(getData()) && (timeStamp - swing.getLastDequeueProcess()) < 6) {
            if(swing.getAverageDelta() < 3 && swing.getAverage() < 180) {
                if((vl = Math.min(12, vl + 1)) > 4) {
                    flag(swing.getAverage() + "=" + swing.getLastAverage() + ", vl=" + vl, true, true, vl > 8 ? AlertTier.HIGH : AlertTier.LIKELY);
                }
            } else vl-= vl > 0 ? 1 : 0;
        }
    }


    @Override
    public void onBukkitEvent(Event event) {

    }
}
