package dev.brighten.anticheat.check.impl.movement.nofall;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "NoFall (B)", description = "Compares the server calculated ground to client calculated ground.",
        checkType = CheckType.NOFALL, punishVL = 12)
@Cancellable
public class NoFallB extends Check {

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(packet.isPos()) {
            if(data.playerInfo.serverGround != data.playerInfo.clientGround
                    && data.playerInfo.climbTicks.value() == 0
                    && !data.playerInfo.generalCancel
                    && (data.playerInfo.deltaY != 0 || data.playerInfo.deltaXZ > 0)
                    && data.playerInfo.lastBlockPlace.hasPassed(20)
                    && data.playerInfo.lastVelocity.hasPassed(40)
                    && data.playerInfo.liquidTicks.value() == 0) {
                if(vl++ > 5) {
                    flag("server=" + data.playerInfo.serverGround + " client=" + data.playerInfo.clientGround);
                }
            } else vl-= vl > 0 ? 1 : 0;
            debug("server=" + data.playerInfo.serverGround + " client=" + data.playerInfo.clientGround
                    + " vl=" + vl + " loaded=" + data.playerInfo.worldLoaded + " cancel=" + data.playerInfo.generalCancel);
        }
    }
}
