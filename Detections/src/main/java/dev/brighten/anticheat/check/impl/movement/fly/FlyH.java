package dev.brighten.anticheat.check.impl.movement.fly;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;

@CheckInfo(name = "Fly (H)", description = "Checks for invalid downwards accelerations", checkType = CheckType.FLIGHT,
        punishVL = 10, executable = true)
@Cancellable
public class FlyH extends Check {

    private int buffer;

    //Electrum is sexy if he was of age.
    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(data.playerInfo.lastVelocity.isNotPassed(20)
                || !data.playerInfo.checkMovement
                || data.playerInfo.canFly
                || data.playerInfo.doingBlockUpdate
                || data.playerInfo.doingTeleport
                || data.playerInfo.lastTeleportTimer.isNotPassed(2)
                || data.playerInfo.creative
                || data.blockInfo.blocksAbove)
            return;

        final double ldeltaY = data.playerInfo.lDeltaY, deltaY = data.playerInfo.deltaY;

        if(Math.abs(deltaY + ldeltaY) < 0.05
                && data.playerInfo.lastHalfBlock.isPassed(2)
                && data.playerInfo.slimeTimer.isPassed(5)
                && Math.abs(deltaY) > 0.2) {
            buffer+=15;
            if(buffer > 20) {
                vl++;
                flag("dy=%.1f ldy=%.1f t=same", deltaY, ldeltaY);
                buffer = 20; //Making sure the buffer doesn't go too high
            }
        } else if(buffer > 0) buffer--;
    }
}
