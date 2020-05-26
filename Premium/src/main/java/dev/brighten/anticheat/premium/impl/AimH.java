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
        developer = true, checkType = CheckType.AIM)
public class AimH extends Check {

    private int buffer;
    private EvictingList<Float> deltaXes = new EvictingList<>(20);

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(packet.isLook()) {
            double delta = MathUtils.getDelta(data.moveProcessor.sensitivityX, data.moveProcessor.sensitivityY);

            if(data.moveProcessor.yawGcdList.size() < 30) return;

            deltaXes.add(data.moveProcessor.deltaX);

            if(deltaXes.size() >= 20) {
                val o = MiscUtils.getOutliers(deltaXes);

                val size = o.one.size() + o.two.size();

                if(size <= 1 && data.moveProcessor.deltaY <= 1) {
                    if(++buffer > 8) {
                        vl++;
                        flag(20 * 60, "o=%v", size);
                    }
                } else buffer = 0;

                debug("low=%v high=%v buffer=%v", o.one.size(), o.two.size(), buffer);
            }

            debug("delta=%v.2 buffer=%v senx=%v.3", delta, buffer, data.moveProcessor.sensitivityX);
        }
    }
}