package dev.brighten.anticheat.check.impl.packets.badpackets;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "BadPackets (O)", description = "Ensures that a player is not using an inventory move.",
        checkType = CheckType.BADPACKETS, punishVL = 10, developer = true)
public class BadPacketsO extends Check {

    private long useStamp;

    @Packet
    public void onFlying(WrappedInArmAnimationPacket packet, long timeStamp) {
        if(useStamp > timeStamp) {
            vl++;
            flag("use="+ useStamp + " arm=" + timeStamp);
        }
    }

    @Packet
    public void onWindow(WrappedInUseEntityPacket packet, long timeStamp) {
        useStamp = timeStamp;
    }
}
