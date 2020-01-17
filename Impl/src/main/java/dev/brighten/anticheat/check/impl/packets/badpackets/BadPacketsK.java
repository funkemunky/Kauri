package dev.brighten.anticheat.check.impl.packets.badpackets;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "BadPackets (K)", description = "Checks for spamming of arm animation packets",
        checkType = CheckType.BADPACKETS, punishVL = 150)
public class BadPacketsK extends Check {

    private long lastArm;
    @Packet
    public void onArm(WrappedInArmAnimationPacket packet, long timeStamp) {
        long delta = timeStamp - lastArm;

        if(delta < 5) {
            if(vl++ > 75) {
                flag("delta=" + delta);
            }
        } else vl-= vl > 0 ? 5 : 0;
    }
}
