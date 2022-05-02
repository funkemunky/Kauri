package dev.brighten.anticheat.check.impl.movement.fly;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;

@CheckInfo(name = "Fly (E)", description = "Looks for consistent vertical acceleration",
        checkType = CheckType.FLIGHT, punishVL = 8, executable = true)
@Cancellable
public class FlyE extends Check {

    private int buffer;
    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(!packet.isPos() || data.playerInfo.flightCancel || data.excuseNextFlying) return;

        double accel = Math.abs(data.playerInfo.deltaY - data.playerInfo.lDeltaY);

        if(accel < 0.01 && Math.abs(data.playerInfo.deltaY) < 1.5
                && data.playerInfo.lastTeleportTimer.isPassed(2)
                && data.playerInfo.lastRespawnTimer.isPassed(20)
                && !data.playerInfo.doingBlockUpdate
                && !data.playerInfo.clientGround && !data.playerInfo.lClientGround) {
            buffer+= 4;

            if(buffer > 15) {
                vl++;
                flag("accel=%s deltaY=%.3f buffer=%s", accel, data.playerInfo.deltaY, buffer);
            }
        } else if(buffer > 0) buffer--;
    }
}
