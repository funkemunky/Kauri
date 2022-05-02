package dev.brighten.anticheat.check.impl.combat.autoclicker;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Autoclicker (A)", description = "A fast click check.", checkType = CheckType.AUTOCLICKER,
        punishVL = 2, executable = true, maxVersion = ProtocolVersion.V1_8_9)
@Cancellable(cancelType = CancelType.INTERACT)
public class AutoclickerA extends Check {

    private int flyingTicks, cps;

    @Setting(name = "cpsToFlag")
    private static int cpsToFlag = 22;

    @Setting(name = "cpsToBan")
    private static int cpsToBan = 28;

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        flyingTicks++;
        if(flyingTicks >= 20) {
            if(cps > cpsToFlag) {
                if(cps > cpsToBan) vl++;
                flag("cps=%s", cps);
            }
            debug("cps=%s", cps);

            flyingTicks = cps = 0;
        }
    }

    @Packet
    public void onArmAnimation(WrappedInArmAnimationPacket packet) {
        if(!data.playerInfo.breakingBlock
                && data.playerInfo.lastBrokenBlock.isPassed(5)
                && data.playerInfo.lastBlockDigPacket.isPassed(1)
                && data.playerInfo.lastBlockPlacePacket.isPassed(1))
            cps++;
        debug("breaking=%s lastBroken=%s", data.playerInfo.breakingBlock,
                data.playerInfo.lastBrokenBlock.getPassed());
    }
}