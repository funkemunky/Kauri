package cc.funkemunky.anticheat.impl.checks.combat.autoclicker;

import cc.funkemunky.anticheat.api.checks.*;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import org.bukkit.event.Event;

@Init
@CheckInfo(name = "Autoclicker (Type H)", description = "A combined autoclicker check.", type = CheckType.AUTOCLICKER, cancelType = CancelType.INTERACT, maxVL = 40)
@Packets(packets = {Packet.Client.ARM_ANIMATION})
public class AutoclickerH extends Check {

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(getData().getTypeC().getVerbose() > 4 && getData().getTypeD().getVerbose() > 4 && getData().getTypeH().getVerbose() > 4) {
            flag("combined= [" + getData().getTypeC().getVerbose() + ", " + getData().getTypeD().getVerbose() + ", " + getData().getTypeH().getVerbose() + "]", true, true, AlertTier.HIGH);
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
