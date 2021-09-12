package dev.brighten.anticheat.check.impl.movement.nofall;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import org.bukkit.event.EventHandler;

@CheckInfo(name = "NoFall (B)", description = "Looks for ground spoofing",
        checkType = CheckType.NOFALL, punishVL = 12, executable = false, developer = true)
@Cancellable
public class NoFallB extends Check {

    private static final double divisor = 1 / 64.;
    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timestamp) {
        if(!data.playerInfo.checkMovement
                || timestamp - data.creation < 2000L) return; // If we are waiting for them to teleport, don't check.

        // If they are saying they are on the ground
        if(data.playerInfo.clientGround
                //And are no where near blocks
                && !data.blockInfo.blocksBelow && !data.blockInfo.blocksNear
                //And didn't collide with ghost blocks recently
                && data.playerInfo.lastGhostCollision.isPassed(20)) {
            vl++;
            flag("T=SPOOF_GROUND dy=%.2f y=%.1f", data.playerInfo.deltaY, data.playerInfo.to.y);
        }

        // If they are saying they are on the ground
        if(!data.playerInfo.clientGround
                // Their bounding box is on the ground
                && (data.playerInfo.serverGround
                && data.playerInfo.lastTeleportTimer.isPassed(1)
                // And we can verify that their behavior is indicative of being grounded
                && (data.playerInfo.to.y % divisor < 1E-6
                        || Math.abs(data.playerInfo.deltaY) < 0.003))) {
            vl++;
            flag("T=SPOOF_AIR dy=%.2f y=%.1f", data.playerInfo.deltaY, data.playerInfo.to.y);
        }
    }
}
