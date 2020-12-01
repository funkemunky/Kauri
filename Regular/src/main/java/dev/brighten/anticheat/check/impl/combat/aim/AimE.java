package dev.brighten.anticheat.check.impl.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import lombok.val;

@CheckInfo(name = "Aim (E)", description = "Checks for weird constant pitch.", checkType = CheckType.AIM,
        developer = true, enabled = false)
public class AimE extends Check {

    private float buffer;

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(!packet.isLook()) return;

        val difference = Math.abs(data.moveProcessor.deltaX - data.moveProcessor.lastDeltaX);
        if (data.moveProcessor.deltaY <= 1 && difference > 3 && data.moveProcessor.deltaX >= 20) {
            if(++buffer > 20) {
                vl++;
                flag("x=%v y=%v", data.moveProcessor.deltaX, data.moveProcessor.deltaY);
            }
        } else if(buffer > 0) buffer-= 2;
        debug("x=%v y=%v", data.moveProcessor.deltaX, data.moveProcessor.deltaY);
    }
}


