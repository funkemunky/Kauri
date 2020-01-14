package dev.brighten.anticheat.check.impl.packets.badpackets;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInEntityActionPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.utils.math.cond.MaxInteger;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "BadPackets (O)", description = "Checks for a player not swinging their arm.",
        checkType = CheckType.BADPACKETS, punishVL = 10, developer = true)
public class BadPacketsO extends Check {

    private boolean swung;
    private int attackTicks;

    @Packet
    public void onFlying(WrappedInUseEntityPacket packet) {
        if(!packet.getAction().equals(WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK)) return;
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        swung = false;
        attackTicks = 0;
    }
    @Packet
    public void onFlying(WrappedInArmAnimationPacket packet) {
        swung = true;
    }
}
