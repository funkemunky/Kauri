package dev.brighten.anticheat.premium.impl;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.MiscUtils;
import dev.brighten.api.check.CheckType;
import lombok.val;

@CheckInfo(name = "Aim (H)", description = "Checks for any low outliers in deltayaw.",
        developer = true, checkType = CheckType.AIM, vlToFlag = 9)
public class AimH extends Check {

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(!packet.isLook()) return;

        if(data.moveProcessor.sensXPercent != data.moveProcessor.sensYPercent) return;

        float rx = Math.abs(data.moveProcessor.deltaX) % 1, ry = Math.abs(data.moveProcessor.deltaY) % 1;

        boolean xFlag = (rx > 0.1 && rx < 0.9), yFlag = (ry > 0.1 && ry < 0.9);

        if(xFlag && yFlag && data.playerInfo.cinematicTimer.hasPassed(5)) {
            vl++;
            flag("rx=%v.4 ry=%v.4", rx, ry);
        } else if(vl > 0) vl-= 0.5;

        debug("rx=%v ry=%v", rx, ry);
    }
}