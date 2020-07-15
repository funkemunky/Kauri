package dev.brighten.anticheat.premium.impl.autoclicker;

import cc.funkemunky.api.tinyprotocol.packet.in.*;
import cc.funkemunky.api.utils.Color;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Autoclicker (I)", description = "Checks for improper clicking from the client.",
        checkType = CheckType.AUTOCLICKER, punishVL = 30)
@Cancellable(cancelType = CancelType.INTERACT)
public class AutoclickerI extends Check {

    private boolean sentFlying, sentTrans;

    @Packet
    public void use(WrappedInArmAnimationPacket packet) {
         debug("sentFlying=%v sentTrans=%v", sentFlying, sentTrans);
        if(sentFlying && sentTrans) {
            vl++;
            if(vl > 11) {
                flag("fly=%v trans=%v", sentFlying, sentTrans);
            }
        } else if(vl > 0) vl-= 2;
        sentFlying = sentTrans = false;
    }

    @Packet
    public void onTrans(WrappedInKeepAlivePacket packet) {
        if(Kauri.INSTANCE.keepaliveProcessor.keepAlives.containsKey((int)packet.getTime())) {
            sentFlying = false;
            sentTrans = true;
        }
    }

    @Packet
    public void flying(WrappedInFlyingPacket packet, long timeStamp) {
        sentFlying = true;
    }
}
