package dev.brighten.anticheat.check.impl.packets.badpackets;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "BadPackets (K)", description = "Checks for spamming of arm animation packets",
        checkType = CheckType.BADPACKETS, punishVL = 10)
public class BadPacketsK extends Check {

    private int arm, flying;

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        flying++;
        if(flying >= 5) {
            if(arm > 100) {
                vl+=6;
                flag("arm=%1", arm);
            }
            debug("arm=%1", arm);
            flying = arm = 0;
        }
    }

    @Packet
    public void onArm(WrappedInArmAnimationPacket packet) {
        arm++;
    }
}
