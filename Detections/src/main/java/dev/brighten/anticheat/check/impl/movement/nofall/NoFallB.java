package dev.brighten.anticheat.check.impl.movement.nofall;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;

@CheckInfo(name = "NoFall (B)", description = "Looks for ground spoofing",
        checkType = CheckType.NOFALL, punishVL = 9, executable = true, vlToFlag = 2)
@Cancellable
public class NoFallB extends Check {

    private static final double divisor = 1 / 64.;

    private int airBuffer, groundBuffer;

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timestamp) {
        if(data.playerInfo.doingTeleport
                || data.playerInfo.lastTeleportTimer.isNotPassed(3)
                || data.playerInfo.moveTicks < 2
                || data.playerInfo.canFly
                || data.playerInfo.slimeTimer.isNotPassed(3)
                || data.playerInfo.creative
                || data.playerInfo.climbTimer.isNotPassed(3)
                || !packet.isPos()
                || timestamp - data.creation < 2000L) {
            //Subtracting buffers
            if(groundBuffer > 0) groundBuffer--;
            if(airBuffer > 0) airBuffer--;
            return; // If we are waiting for them to teleport, don't check.
        }

        // If they are saying they are on the ground
        if(data.playerInfo.clientGround
                && !data.playerInfo.doingBlockUpdate
                && !data.playerInfo.serverGround
                && data.playerInfo.to.y % divisor >= 1E-4
                && data.playerInfo.vehicleTimer.isPassed(20)
                && data.playerInfo.lastBlockPlace.isPassed(4)
                //And are no where near blocks
                && !data.blockInfo.blocksBelow && !data.blockInfo.blocksNear
                //And didn't collide with ghost blocks recently
                && data.playerInfo.lastGhostCollision.isPassed(6)) {
            groundBuffer+= 2;

            if(groundBuffer > 14) {
                vl++;
                groundBuffer = 14;
                flag(200, "T=SPOOF_GROUND dy=%.2f y=%.1f", data.playerInfo.deltaY, data.playerInfo.to.y);
            }
            fixMovementBugs();
        } else if(groundBuffer > 0) groundBuffer--;


        final boolean dground = data.playerInfo.to.y % divisor < 1E-4 && data.playerInfo.nearGround;
        // If they are saying they are on the ground
        if(!data.playerInfo.clientGround
                // Their bounding box is on the ground
                && data.playerInfo.vehicleTimer.isPassed(20)
                && ((data.playerInfo.serverGround || data.blockInfo.blocksBelow) && dground)
                && data.playerInfo.lastTeleportTimer.isPassed(2)) {
            if((airBuffer +=10) > 30) {
                vl++;
                flag(200, "T=SPOOF_AIR dy=%.2f y=%.1f", data.playerInfo.deltaY, data.playerInfo.to.y);
            }
        } else if(airBuffer > 0) airBuffer -= 4;

        debug("c=%s s=%s bbelow=%s dg=%s dy=%.4f", data.playerInfo.clientGround, data.playerInfo.serverGround,
                data.blockInfo.blocksBelow, dground, data.playerInfo.deltaY);
    }
}
