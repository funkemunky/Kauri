package dev.brighten.anticheat.check.impl.packets.badpackets;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "BadPackets (O)", description = "Checks for a player not swinging their arm.",
        checkType = CheckType.BADPACKETS, punishVL = 10, developer = true)
public class BadPacketsO extends Check {

    private boolean attacked;

    @Packet
    public void onFlying(WrappedInArmAnimationPacket packet, long timeStamp) {
        if(attacked) {
            vl++;
            flag("ping=%p tps=%t");
        } else vl-= vl > 0 ? 0.005f : 0;
    }

    @Packet
    public void onWindow(WrappedInUseEntityPacket packet) {
        attacked = true;
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        attacked = false;
    }
}
