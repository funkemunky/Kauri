package dev.brighten.anticheat.check.impl.movement.general;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInTransactionPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;

//@CheckInfo(name = "Blink", description = "Invalid lag spikes", checkType = CheckType.GENERAL,
//        devStage = DevStage.ALPHA, vlToFlag = 3)
public class Blink extends Check {

    private int buffer = 0;
    @Packet
    public void onTrans(WrappedInTransactionPacket packet) {
        if(data.playerInfo.lastFlyingTimer.isPassed(
                (data.playerVersion.isAbove(ProtocolVersion.V1_8_9) ? 40 : 25) + data.lagInfo.transPing)) {
            if(++buffer > 2) {
                vl++;
                flag(40, "%s", data.playerInfo.lastFlyingTimer.getPassed());
            }
        } else buffer = 0;
    }
}
