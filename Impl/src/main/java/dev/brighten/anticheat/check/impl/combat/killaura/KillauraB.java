package dev.brighten.anticheat.check.impl.combat.killaura;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInKeepAlivePacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInTransactionPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Killaura (B)", description = "Detects when clients sent use packets at same time as flying packets.",
        checkType = CheckType.KILLAURA, punishVL = 17)
@Cancellable(cancelType = CancelType.ATTACK)
public class KillauraB extends Check {

    private boolean sentFlying, sentTrans;

    @Packet
    public void use(WrappedInUseEntityPacket packet) {
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
    public void flying(WrappedInFlyingPacket packet) {
        sentFlying = true;
    }
}
