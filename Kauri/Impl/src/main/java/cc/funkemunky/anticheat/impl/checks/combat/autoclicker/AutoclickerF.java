package cc.funkemunky.anticheat.impl.checks.combat.autoclicker;

import cc.funkemunky.anticheat.api.checks.*;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import org.bukkit.event.Event;

@Packets(packets = {
        Packet.Client.ARM_ANIMATION,
        Packet.Client.FLYING,
        Packet.Client.POSITION,
        Packet.Client.POSITION_LOOK,
        Packet.Client.LOOK,
        Packet.Client.LEGACY_POSITION,
        Packet.Client.LEGACY_POSITION_LOOK,
        Packet.Client.LEGACY_LOOK})
//@cc.funkemunky.api.utils.Init
@CheckInfo(name = "Autoclicker (Type F)", description = "Looks to see if the CPS is rounded consistently, something only autoclickers could do.", type = CheckType.AUTOCLICKER, cancelType = CancelType.INTERACT, maxVL = 50)
public class AutoclickerF extends Check {

    private long lastTimeStamp;
    private double vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        long delta = timeStamp - lastTimeStamp;
        if(!MiscUtils.shouldReturnArmAnimation(getData()) && delta > 0 && delta < 200) {
            double cps = 1000D / (timeStamp - lastTimeStamp);

            if(cps % 0.5 == 0 || cps % 1 == 0) {
                if(vl++ > 6) {
                    flag("vl=" + vl + " cps=" + cps, false, true, AlertTier.LIKELY);
                }
            } else vl-= vl > 0 ? 0.25 : 0;

            debug("cps=" + cps + " vl=" + vl);
        }

        lastTimeStamp = timeStamp;

    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
