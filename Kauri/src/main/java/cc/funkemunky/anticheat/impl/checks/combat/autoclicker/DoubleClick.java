package cc.funkemunky.anticheat.impl.checks.combat.autoclicker;

import cc.funkemunky.anticheat.api.checks.*;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.TickTimer;
import org.bukkit.event.Event;

@CheckInfo(name = "Autoclicker (DoubleClick)", description = "Checks for double clicking.", type = CheckType.AUTOCLICKER, cancelType = CancelType.INTERACT, executable = false)
@Packets(packets = {
        Packet.Client.ARM_ANIMATION,
        Packet.Client.FLYING,
        Packet.Client.POSITION,
        Packet.Client.POSITION_LOOK,
        Packet.Client.LOOK,
        Packet.Client.LEGACY_POSITION,
        Packet.Client.LEGACY_POSITION_LOOK,
        Packet.Client.LEGACY_LOOK})
//@Init
public class DoubleClick extends Check {

    private long lastFlying, lastArm;
    private TickTimer lastLag = new TickTimer(1);
    private int vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(packetType.equals(Packet.Client.ARM_ANIMATION)) {
            if(timeStamp - lastArm == 0 && lastLag.hasPassed()) {
                if(vl++ > 10) {
                    flag("vl=" + vl, true, true, AlertTier.HIGH);
                } else if(vl > 4) {
                    flag("vl=" + vl, true, true, AlertTier.LIKELY);
                } else {
                    flag("vl=" + vl, true, false, AlertTier.POSSIBLE);
                }
            } else vl-= vl > 0 ? 1 : 0;
            lastArm = timeStamp;
        } else {
            if(timeStamp - lastFlying == 0) {
                lastLag.reset();
            }
            lastFlying = timeStamp;
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
