package dev.brighten.anticheat.premium.impl.autoclicker;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInTransactionPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Autoclicker (I)", description = "Checks for improper clicking from the client.",
        checkType = CheckType.AUTOCLICKER, punishVL = 30)
@Cancellable(cancelType = CancelType.INTERACT)
public class AutoclickerI extends Check {

    private long lastFlying;

    @Packet
    public void use(WrappedInArmAnimationPacket packet, long current) {
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
