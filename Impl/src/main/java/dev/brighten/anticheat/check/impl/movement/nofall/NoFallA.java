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
                : data.playerInfo.deltaY == 0 && data.playerInfo.lDeltaY == 0;

        if(!data.playerInfo.flightCancel
                && !data.blockInfo.onSlime
                && (data.playerInfo.deltaY != 0 || data.playerInfo.deltaXZ > 0)
                && data.playerInfo.blocksAboveTicks.value() == 0
                && timeStamp - data.playerInfo.lastServerPos > 100L
                && flag) {
            vl+= data.lagInfo.lagging || data.lagInfo.lastPacketDrop.hasNotPassed(3) ? 1 : 3;

            if(vl > 2) {
                flag("ground=" + data.playerInfo.clientGround + " deltaY=" + data.playerInfo.deltaY);
            }
        } else vl-= vl > 0 ? 0.2f : 0;

        debug("ground=" + data.playerInfo.clientGround
                + " deltaY=" + data.playerInfo.deltaY + " vl=" + vl);
    }
}