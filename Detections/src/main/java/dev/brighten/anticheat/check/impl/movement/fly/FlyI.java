package dev.brighten.anticheat.check.impl.movement.fly;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;

@CheckInfo(name = "Fly (I)", description = "Checks for bad hovering.",
        checkType = CheckType.FLIGHT, devStage = DevStage.BETA, punishVL = 8)
@Cancellable
public class FlyI extends Check {

    private int buffer;
    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(!packet.isPos()
                || data.playerInfo.generalCancel
                || (data.playerInfo.serverGround
                        || (data.playerInfo.clientGround && data.playerInfo.groundTicks < 3))
                || data.playerInfo.lastToggleFlight.isNotPassed(10)
                || data.playerInfo.lastVelocity.isNotPassed(1)
                || data.playerInfo.doingVelocity
                || data.playerInfo.deltaY != 0
                || data.playerInfo.climbTimer.isNotPassed(2)) {
            buffer = 0;
            debug("Resetting buffer");
        }

        if(++buffer > 6) {
            vl++;
            flag("b=%s a=%s", buffer, data.playerInfo.airTicks);
        }
    }
}
