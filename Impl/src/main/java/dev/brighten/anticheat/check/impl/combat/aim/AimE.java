package dev.brighten.anticheat.check.impl.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.Color;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import lombok.val;

@CheckInfo(name = "Aim (E)", description = "Checks for weird constant pitch.", checkType = CheckType.AIM,
        developer = true)
public class AimE extends Check {

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(!packet.isLook()) return;

        val difference = Math.abs(data.moveProcessor.deltaX - data.moveProcessor.lastDeltaX);
        if (data.moveProcessor.deltaY <= 1 && data.moveProcessor.deltaX >= 20) {
            vl++;
            if(vl > 20) {
                flag("x=%v y=%v", data.moveProcessor.deltaX, data.moveProcessor.deltaY);
            }
        } else if(vl > 0) vl-= 2;
        debug("x=%v y=%v", data.moveProcessor.deltaX, data.moveProcessor.deltaY);
    }
}


