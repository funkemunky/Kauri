package dev.brighten.anticheat.check.impl.combat.killaura;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.api.check.CheckType;

@Cancellable(cancelType = CancelType.ATTACK)
@CheckInfo(name = "Killaura (C)", description = "Checks for noswing modules on clients.",
        checkType = CheckType.KILLAURA, punishVL = 20)
public class KillauraC extends Check {

    private long lastArm;
    @Packet
    public void onArm(WrappedInArmAnimationPacket packet, long timeStamp) {
        lastArm = timeStamp;
    }

    @Packet
    public void onUse(WrappedInUseEntityPacket packet, long timeStamp) {
        if(!packet.getAction().equals(WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK)
                || data.playerVersion.isOrAbove(ProtocolVersion.V1_9)) return;

        long delta = timeStamp - lastArm;

        if(delta > 200
                && data.lagInfo.lastPacketDrop.hasPassed(10)
                && !data.lagInfo.lagging) {
            vl++;
            if(vl > 3) {
                flag("ms:" + delta);
            }
        } else vl-= vl > 0 ? data.lagInfo.lagging
                || data.lagInfo.lastPacketDrop.hasNotPassed(20) ? 0.25f : 0.1f : 0;

        debug("ms=" + delta);
    }
}
