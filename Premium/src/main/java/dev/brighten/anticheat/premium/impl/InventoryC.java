package dev.brighten.anticheat.premium.impl;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInTransactionPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInWindowClickPacket;
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
            vl++;
            if(vl > 11) {
                flag("fly=%v trans=%v", sentFlying, sentTrans);
            }
        } else if(vl > 0) vl--;
        sentFlying = sentTrans = false;
    }

    @Packet
    public void onTrans(WrappedInTransactionPacket packet) {
        if(Kauri.INSTANCE.keepaliveProcessor.keepAlives.containsKey(packet.getAction())) {
            sentFlying = false;
            sentTrans = true;
        }
    }

    @Packet
    public void flying(WrappedInFlyingPacket packet, long timeStamp) {
        sentFlying = true;
    }
}
