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

    private boolean swung;

    @Packet
    public void onUse(WrappedInUseEntityPacket packet, long timeStamp) {
        if(packet.getAction().equals(WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK)
                && !swung && data.lagInfo.lastPacketDrop.hasPassed(4)) {
            vl++;
            if(vl > 4) {
                flag("[did not swing] ping=%p tps=%t");
            }
        } else vl-= vl > 0 ? (data.lagInfo.lagging ? 0.5f : 0.025f) : 0;
    }

    @Packet
    public void onArm(WrappedInArmAnimationPacket packet) {
        swung = true;
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        swung = false;
    }
}
