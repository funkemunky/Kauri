package dev.brighten.anticheat.check.impl.movement.general;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.utils.trans.WrappedServerboundTransactionPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.Packet;

//@CheckInfo(name = "Blink", description = "Invalid lag spikes", checkType = CheckType.GENERAL,
//        devStage = DevStage.ALPHA, vlToFlag = 3)
public class Blink extends Check {

    private int buffer = 0;
    @Packet
    public void onTrans(WrappedServerboundTransactionPacket packet) {
        if(data.playerInfo.lastFlyingTimer.isPassed(
                (data.playerVersion.isAbove(ProtocolVersion.V1_8_9) ? 40 : 25) + data.lagInfo.transPing)) {
            if(++buffer > 2) {
                vl++;
                flag(40, "%s", data.playerInfo.lastFlyingTimer.getPassed());
            }
        } else buffer = 0;
    }
}
