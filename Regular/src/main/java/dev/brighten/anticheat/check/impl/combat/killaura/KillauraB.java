package dev.brighten.anticheat.check.impl.combat.killaura;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Killaura (B)", description = "Detects when clients sent use packets at same time as flying packets.",
        checkType = CheckType.KILLAURA, punishVL = 17)
@Cancellable(cancelType = CancelType.ATTACK)
public class KillauraB extends Check {

    private long lastFlying;

    @Packet
    public void use(WrappedInUseEntityPacket packet, long current) {
        if(current - lastFlying < 10) {
            vl++;
            if(vl > 11) {
                flag("delta=0");
            }
        } else if(vl > 0) vl--;
    }

    @Packet
    public void flying(WrappedInFlyingPacket packet, long current) {
        lastFlying = current;
    }
}
