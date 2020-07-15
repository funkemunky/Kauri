package dev.brighten.anticheat.premium.impl;

import cc.funkemunky.api.tinyprotocol.packet.in.*;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Inventory (C)", description = "Checks for invalid window clicks.", checkType = CheckType.INVENTORY)
public class InventoryC extends Check {

    private boolean sentFlying, sentTrans;

    @Packet
    public void use(WrappedInWindowClickPacket packet) {
         debug("sentFlying=%v sentTrans=%v", sentFlying, sentTrans);
        if(sentFlying && sentTrans) {
            vl+= 2;
            if(vl > 11) {
                flag("fly=%v trans=%v", sentFlying, sentTrans);
            }
        } else if(vl > 0) vl--;
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
