package dev.brighten.anticheat.check.impl.combat.killaura;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Killaura (B)", description = "Detects when clients sent use packets at same time as flying packets.",
        checkType = CheckType.KILLAURA, punishVL = 40)
@Cancellable(cancelType = CancelType.ATTACK)
public class KillauraB extends Check {

    private long lastFlying;

    @Packet
    public void use(WrappedInUseEntityPacket packet, long timeStamp) {
        if(!packet.getAction().equals(WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK)) return;
        long delta = timeStamp - lastFlying;
        if(delta < 2) {
            if(!data.lagInfo.lagging) {
                if(vl++ > 15) {
                    flag("delta=" + delta);
                }
            } vl-= vl > 0 ? 0.1 : 0;
        } else vl-= vl > 0 ? 1 : 0;
        debug("lagging=" + data.lagInfo.lagging + " vl=" + vl + " delta=" + delta);
    }

    @Packet
    public void flying(WrappedInFlyingPacket packet, long timeStamp) {
        lastFlying = timeStamp;
    }
}
