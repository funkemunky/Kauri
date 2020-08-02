package dev.brighten.anticheat.check.impl.packets.badpackets;

import cc.funkemunky.api.tinyprotocol.packet.in.*;
import cc.funkemunky.api.utils.math.cond.MaxDouble;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "BadPackets (C)", description = "Checks for players who send slot packets at the same time as flying.",
        checkType = CheckType.BADPACKETS, punishVL = 20)
@Cancellable
public class BadPacketsC extends Check {

    private boolean sentFlying, sentTrans;

    @Packet
    public void use(WrappedInHeldItemSlotPacket packet) {
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
