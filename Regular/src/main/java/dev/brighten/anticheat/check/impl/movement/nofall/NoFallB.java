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

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(!packet.isPos() || !data.playerInfo.worldLoaded
                || data.playerInfo.generalCancel
                || data.playerInfo.serverPos
                || data.playerInfo.lastTeleportTimer.isNotPassed(2)
                || data.playerInfo.lastRespawnTimer.isNotPassed(10))
            return;

        if(!data.playerInfo.serverGround
                && !data.playerInfo.nearGround && data.playerInfo.clientGround && !data.blockInfo.onSlime
                && data.playerInfo.lastHalfBlock.isPassed(3)) {
            if(++vl > 2) {
                flag("c=%s s=%s", packet.isGround(), data.playerInfo.serverGround);
            }
        } else if(vl > 0) vl-= 0.5;

        debug("c=%s s=%s", packet.isGround(), data.playerInfo.serverGround);
    }
}
