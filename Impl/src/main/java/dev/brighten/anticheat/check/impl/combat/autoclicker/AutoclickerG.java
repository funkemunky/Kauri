package dev.brighten.anticheat.check.impl.combat.autoclicker;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Autoclicker (G)", description = "Checks for weird doubleclicking from internal autoclickers.",
        checkType = CheckType.AUTOCLICKER, punishVL = 10, developer = true)
public class AutoclickerG extends Check {

    private boolean sent;
    private int timesSent;

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        sent = false;
        timesSent = 0;
        vl-= vl > 0 ? 0.0025f : 0;
    }

    @Packet
    public void onArm(WrappedInArmAnimationPacket packet) {
        timesSent++;
        if(sent && data.lagInfo.lastPacketDrop.hasPassed(1)) {
            if(vl++ > 1) {
                flag("sent=%1", timesSent);
            }
            debug("sent=%1 vl=%2 lastDrop=%3", timesSent, vl, data.lagInfo.lastPacketDrop.getPassed());
        } else sent = true;
    }
}
