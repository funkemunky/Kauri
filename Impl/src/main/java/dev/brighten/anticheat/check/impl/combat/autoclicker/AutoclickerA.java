package dev.brighten.anticheat.check.impl.combat.autoclicker;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.types.MathHelper;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Autoclicker (A)", description = "A fast click check.", checkType = CheckType.AUTOCLICKER,
        punishVL = 2, maxVersion = ProtocolVersion.V1_8_9)
@Cancellable(cancelType = CancelType.INTERACT)
public class AutoclickerA extends Check {

    @Setting(name = "cpsToFlag")
    private static int cpsToFlag = 22;

    @Setting(name = "cpsToBan")
    private static int cpsToBan = 28;

    @Packet
    public void onArmAnimation(WrappedInArmAnimationPacket packet) {
        if(!data.playerInfo.breakingBlock
                && data.playerInfo.lastBrokenBlock.hasPassed(5)
                && data.playerInfo.lastBlockPlace.hasPassed(2)) {
            int cps = MathHelper.floor((1000D / data.clickProcessor.getMean()));
            if(cps > cpsToFlag) {
                if(cps > cpsToBan) vl++;
                flag("cps=%v", cps);
            }
            debug("cps=%v", cps);
        }
    }
}
