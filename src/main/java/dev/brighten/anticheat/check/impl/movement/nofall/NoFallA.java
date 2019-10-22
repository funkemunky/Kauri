package dev.brighten.anticheat.check.impl.movement.nofall;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.CheckType;
import dev.brighten.anticheat.check.api.Packet;

@CheckInfo(name = "NoFall (A)", description = "Checks to make sure the ground packet from the client is legit",
        checkType = CheckType.BADPACKETS, punishVL = 20, executable = false)
public class NoFallA extends Check {

    @Packet
    public void onPacket(WrappedInFlyingPacket packet) {
        if(packet.isPos()) {
            if((data.playerInfo.deltaXZ > 0 || data.playerInfo.deltaY > 0)
                    && !data.playerInfo.serverIsFlying
                    && !data.playerInfo.serverPos
                    && !data.playerInfo.onLadder
                    && MathUtils.getDelta(0.5, Math.abs(data.playerInfo.deltaY)) > 1E-4
                    && data.playerInfo.halfBlockTicks == 0
                    && (data.playerInfo.deltaY == 0 && !data.playerInfo.clientGround)
                    || (data.playerInfo.deltaY != 0 && data.playerInfo.clientGround)) {
                if(vl++ > 5) {
                    flag("deltaY=" + data.playerInfo.deltaY + " client=" + data.playerInfo.clientGround);
                }
            } else vl-= vl > 0 ? 1 : 0;
        }
    }
}
