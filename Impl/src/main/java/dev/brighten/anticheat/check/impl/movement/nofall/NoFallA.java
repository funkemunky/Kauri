package dev.brighten.anticheat.check.impl.movement.nofall;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "NoFall (A)", description = "Checks to make sure the ground packet from the client is legit",
        checkType = CheckType.NOFALL, punishVL = 20, executable = false)
@Cancellable
public class NoFallA extends Check {

    @Packet
    public void onPacket(WrappedInFlyingPacket packet, long timeStamp) {
        if(!packet.isPos()) return;

        boolean flag = data.playerInfo.clientGround
                ? data.playerInfo.deltaY != 0 && !data.playerInfo.serverGround
                && data.playerInfo.lastBlockPlace.hasPassed(10)
                : data.playerInfo.deltaY == 0 && data.playerInfo.lDeltaY == 0;

        if(!data.playerInfo.flightCancel
                && data.playerInfo.lastHalfBlock.hasPassed(4)
                && !data.blockInfo.onSlime
                && data.playerInfo.lastVelocity.hasPassed(4)
                && (data.playerInfo.deltaY != 0 || data.playerInfo.deltaXZ > 0)
                && data.playerInfo.blockAboveTimer.hasPassed(10)
                && flag) {
            vl+= data.lagInfo.lagging
                    || data.lagInfo.lastPacketDrop.hasNotPassed(1)
                    || data.playerInfo.nearGround
                    || data.blockInfo.blocksNear
                    ? 1 : data.playerInfo.clientGround ? 2 : 3;

            if(vl > 2) {
                flag("ground=" + data.playerInfo.clientGround + " deltaY=" + data.playerInfo.deltaY);
            }
        } else vl-= vl > 0 ? 0.2f : 0;

        debug("ground=" + data.playerInfo.clientGround
                + " deltaY=" + data.playerInfo.deltaY + " vl=" + vl);
    }
}