package dev.brighten.anticheat.check.impl.movement.nofall;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "NoFall (B)", description = "A very simple NoFall check.",
        checkType = CheckType.NOFALL, punishVL = 12, executable = false, developer = true)
@Cancellable
public class NoFallB extends Check {

    private static double GROUND = 1 / 64d;
    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(!packet.isPos() || !data.playerInfo.worldLoaded
                || data.playerInfo.generalCancel
                || data.playerInfo.lastTeleportTimer.hasNotPassed(1)
                || data.playerInfo.lastRespawnTimer.hasNotPassed(1))
            return;

        boolean ground = data.playerInfo.to.y % GROUND == 0;

        if(ground != packet.isGround() && !data.blockInfo.onSlime
                && data.playerInfo.lastHalfBlock.hasPassed(3)) {
            if(++vl > 2) {
                flag("c=%v s=%v", packet.isGround(), ground);
            }
        } else if(vl > 0) vl-= 0.5;

        debug("c=%v s=%v", packet.isGround(), ground);
    }
}
