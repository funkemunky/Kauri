package dev.brighten.anticheat.check.impl.movement.nofall;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.CheckType;
import dev.brighten.anticheat.check.api.Packet;

@CheckInfo(name = "NoFall (A)", description = "Checks to make sure the ground packet from the client is legit",
        checkType = CheckType.BADPACKETS, punishVL = 20, executable = false)
public class NoFallA extends Check {

    private int groundTicks, airTicks;
    @Packet
    public void onPacket(WrappedInFlyingPacket packet) {
        if(!packet.isPos()) return;

        boolean flag = packet.isGround() ? data.playerInfo.deltaY != 0 : data.playerInfo.deltaY == 0;

        groundTicks = packet.isGround() ? groundTicks + 1 : 0;
        airTicks = !packet.isGround() ? airTicks + 1 : 0;

        if(!data.playerInfo.flying && !data.playerInfo.canFly && flag && (groundTicks > 2 || airTicks > 3)) {

        }
    }
}