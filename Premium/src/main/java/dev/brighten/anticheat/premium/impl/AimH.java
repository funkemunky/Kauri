package dev.brighten.anticheat.premium.impl;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Aim (H)", description = "Checks the sensitivity difference.",
        developer = true, checkType = CheckType.AIM)
public class AimH extends Check {

    private int buffer;
    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(packet.isLook()) {
            double delta = MathUtils.getDelta(data.moveProcessor.sensitivityX, data.moveProcessor.sensitivityY);

            if(data.moveProcessor.yawGcdList.size() < 30) return;

            if(delta > 0.228 ||  data.moveProcessor.sensitivityX > 2.0001 || data.moveProcessor.sensitivityY > 2.0001) {
                if(++buffer > 40) {
                    vl++;
                    flag("sens=%v delta=%v.2 buffer=%v", data.moveProcessor.sensXPercent, delta, buffer);
                }
            } else buffer = 0;

            debug("delta=%v.2 buffer=%v senx=%v.3", delta, buffer, data.moveProcessor.sensitivityX);
        }
    }
}