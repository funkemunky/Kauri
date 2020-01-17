package dev.brighten.anticheat.check.impl.combat.autoclicker;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockDigPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockPlacePacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Autoclicker (G)", description = "Checks for weird doubleclicking from internal autoclickers.",
        checkType = CheckType.AUTOCLICKER, punishVL = 10, developer = true)
public class AutoclickerG extends Check {

    private boolean sent;
    private int timesSent;
    private long lastFlying;

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        sent = false;
        timesSent = 0;
        vl-= vl > 0 ? 0.0025f : 0;
        lastFlying = timeStamp;
    }

    @Packet
    public void onBlock(WrappedInBlockDigPacket packet) {
        sent = false;
        timesSent = 0;
    }

    @Packet
    public void onBlockPlace(WrappedInBlockPlacePacket packet) {
        sent = false;
        timesSent = 0;
    }

    @Packet
    public void onArm(WrappedInArmAnimationPacket packet, long timeStamp) {
        timesSent++;
        if(sent && data.lagInfo.lastPacketDrop.hasPassed(1)
                && data.playerInfo.lastBrokenBlock.hasPassed(2)
                && MathUtils.getDelta(timeStamp - lastFlying, 50) < 20) {
            if(vl++ > 1) {
                flag("sent=%1", timesSent);
            }
            debug("sent=%1 vl=%2 lastDrop=%3", timesSent, vl, data.lagInfo.lastPacketDrop.getPassed());
        } else sent = true;
    }
}
