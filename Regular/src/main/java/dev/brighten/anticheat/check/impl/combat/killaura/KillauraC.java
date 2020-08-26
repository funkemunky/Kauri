package dev.brighten.anticheat.check.impl.combat.killaura;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.api.check.CheckType;

@Cancellable(cancelType = CancelType.ATTACK)
@CheckInfo(name = "Killaura (C)", description = "Checks for noswing modules on clients.",
        checkType = CheckType.KILLAURA, punishVL = 20, maxVersion = ProtocolVersion.V1_8_9)
public class KillauraC extends Check {

    private long lastArm;

    @Packet
    public void onArm(WrappedInArmAnimationPacket packet, long timeStamp) {
        lastArm = timeStamp;
    }

    @Packet
    public void onUse(WrappedInUseEntityPacket packet, long timeStamp) {
        if(!packet.getAction().equals(WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK)) return;

        long delta = timeStamp - lastArm;

        if(delta > 1000L
                && data.lagInfo.lastPacketDrop.hasPassed(10)
                && !data.lagInfo.lagging) {
            vl++;
            if(vl > 3) {
                flag("ms:" + delta);
            }
        } else if(vl > 0) vl-= 0.5f;

        debug("ms=" + delta);
    }
}
