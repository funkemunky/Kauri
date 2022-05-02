package dev.brighten.anticheat.check.impl.packet.badpacket;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInEntityActionPacket;
import cc.funkemunky.api.utils.math.cond.MaxInteger;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "BadPackets (B)", description = "Checks for the spamming of sneak changes.",
        checkType = CheckType.BADPACKETS, punishVL = 10, executable = true)
@Cancellable
public class BadPacketsB extends Check {

    private long lastSneak;
    private MaxInteger ticks = new MaxInteger(Integer.MAX_VALUE);
    @Packet
    public void onPlace(WrappedInEntityActionPacket action, long timeStamp) {
        if(action.getAction().name().contains("SNEAK")) {
            if(timeStamp - lastSneak <= 10) {
                ticks.add();
                if(ticks.value() > 80) {
                    vl++;
                    flag("ticks=%s", ticks.value());
                }
            } else ticks.subtract(ticks.value() > 40 ? 8 : 4);
            lastSneak = timeStamp;
        }
        debug(action.getAction().name());
    }
}
