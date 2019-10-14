package dev.brighten.anticheat.check.impl.movement.nofall;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.CheckType;
import dev.brighten.anticheat.check.api.Packet;

@CheckInfo(name = "NoFall (B)", description = "Compares the server calculated ground to client calculated ground.",
        checkType = CheckType.BADPACKETS)
public class NoFallB extends Check {

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(packet.isPos()) {
            if(data.predictionService.onGround != data.playerInfo.clientGround && !data.playerInfo.generalCancel) {
                if(vl++ > 5) {
                    flag("server=" + data.predictionService.onGround + " client=" + data.playerInfo.clientGround);
                }
            } else vl-= vl > 0 ? 1 : 0;
            debug("server=" + data.predictionService.onGround + " client=" + data.playerInfo.clientGround + " vl=" + vl);
        }
    }
}
