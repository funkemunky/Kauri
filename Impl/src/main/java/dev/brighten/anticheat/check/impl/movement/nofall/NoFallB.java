package dev.brighten.anticheat.check.impl.movement.nofall;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "NoFall (B)", description = "",
        checkType = CheckType.NOFALL, punishVL = 12, executable = false, developer = true)
@Cancellable
public class NoFallB extends Check {

    private static double GROUND = 1 / 64d;
    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(!packet.isPos() || !data.playerInfo.worldLoaded || data.playerInfo.serverPos
                || data.playerInfo.lastTeleportTimer.hasPassed(1)
                || data.playerInfo.lastRespawnTimer.hasNotPassed(1))
            return;

        boolean ground = data.playerInfo.to.y % GROUND == 0;

        if(ground != packet.isGround()) {
            vl++;
            flag("c=%v s=%v", packet.isGround(), ground);
        }

        debug("c=%v s=%v", packet.isGround(), ground);
    }
}
