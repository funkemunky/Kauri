package dev.brighten.anticheat.check.impl.movement.fly;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.CheckType;
import dev.brighten.anticheat.check.api.Packet;

@CheckInfo(name = "Fly (C)", description = "Checks for changes in y motion in air.",
        checkType = CheckType.FLIGHT, punishVL = 12, executable = false)
public class FlyC extends Check {

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(!packet.isPos()) return;

        if(!data.playerInfo.nearGround
                && data.playerInfo.lastBlockPlace.hasPassed(20)
                && !data.playerInfo.flightCancel
                && data.playerInfo.lastVelocity.hasPassed(5)
                && data.playerInfo.airTicks > 3
                && data.playerInfo.deltaY > data.playerInfo.lDeltaY + 1E-4) {
            if((vl+=2) > 3) {
                flag("y=" + data.playerInfo.deltaY + " lY=" +  data.playerInfo.lDeltaY);
            }
        } else vl-= vl > 0 ? 0.04 : 0;
    }
}
