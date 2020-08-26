package dev.brighten.anticheat.premium.impl.autoclicker;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Autoclicker (I)", description = "Checks for improper clicking from the client.",
        checkType = CheckType.AUTOCLICKER, punishVL = 30)
@Cancellable(cancelType = CancelType.INTERACT)
public class AutoclickerI extends Check {

    private long lastFlying, lastArm;

    @Packet
    public void onClick(WrappedInArmAnimationPacket packet, long timeStamp) {
        long deltaFlying = timeStamp - lastFlying;

        if(deltaFlying <= 0 && timeStamp - lastArm > 3) {
            if(++vl > 5) {
                flag("invalid click");
            }
        } else if(vl > 0) vl-= 0.5f;

        debug("delta=%v deltaArm=%v vl=%v", deltaFlying, timeStamp - lastArm, vl);
        lastArm = timeStamp;
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        lastFlying = timeStamp;
    }
}
