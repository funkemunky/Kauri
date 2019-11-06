package dev.brighten.anticheat.check.impl.movement.fly;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.CheckType;
import dev.brighten.anticheat.check.api.Packet;

@CheckInfo(name = "Fly (E)", description = "Checks for FastLadders",
        checkType = CheckType.FLIGHT, executable = false, punishVL = 20)
public class FlyE extends Check {

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(packet.isPos()
                && !data.playerInfo.lastVelocity.hasPassed(10)
                && data.playerInfo.onLadder
                && !data.playerInfo.generalCancel) {
            if(data.playerInfo.deltaY > 0.144) {
                if((vl+=(data.playerInfo.deltaY > data.playerInfo.jumpHeight ? 4 : 1)) > 8) {
                    flag("deltaY=" + data.playerInfo.deltaY);
                }
            } else vl-= vl > 0 ? 0.2f : 0;

            debug("deltaY=" + data.playerInfo.deltaY + " vl=" + vl);
        } else vl-= vl > 0 ? 0.02f : 0;
    }
}
