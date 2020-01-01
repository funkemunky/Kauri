package dev.brighten.anticheat.check.impl.movement.nofall;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "NoFall (B)", description = "Compares the server calculated ground to client calculated ground.",
        checkType = CheckType.NOFALL, punishVL = 12)
public class NoFallB extends Check {

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        if(packet.isPos()) {
            if((data.playerInfo.serverGround && data.blockInfo.collidesVertically) != data.playerInfo.clientGround
                    && data.playerInfo.climbTicks.value() == 0
                    && !data.playerInfo.generalCancel
                    && data.playerInfo.liquidTicks.value() == 0) {
                if(vl++ > 5) {
                    flag("server=" + data.playerInfo.serverGround + " client=" + data.playerInfo.clientGround);
                }
            } else vl-= vl > 0 ? 1 : 0;
            debug("server=" + data.playerInfo.serverGround + " client=" + data.playerInfo.clientGround + " vl=" + vl);
        }
    }
}
