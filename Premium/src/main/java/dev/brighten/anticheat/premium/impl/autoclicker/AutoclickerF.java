package dev.brighten.anticheat.premium.impl.autoclicker;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.MiscUtils;
import dev.brighten.api.check.CheckType;
import lombok.val;

import java.util.ArrayList;
import java.util.List;

@CheckInfo(name = "Autoclicker (F)", description = "Checks for a constant skew of values.", developer = true,
        checkType = CheckType.AUTOCLICKER, maxVersion = ProtocolVersion.V1_8_9)
public class AutoclickerF extends Check {

    public int flying, buffer;

    public List<Float> cpsList = new ArrayList<>();
    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        flying++;
    }

    @Packet
    public void onArm(WrappedInArmAnimationPacket packet) {
        float cps = 20.f / flying;

        cpsList.add(cps);

        if(cpsList.size() == 20) {
            val skewness = MiscUtils.getSkewness(cpsList);
            val variance = MiscUtils.stdev(cpsList);

            if(variance > 2.5 && Math.abs(skewness) < 0.1) {
                buffer+= 4;
                if(buffer > 6) {
                    vl++;
                    flag("skew=%v.2 variance=%v.3", skewness, variance);
                }
            } else if(buffer > 0) buffer--;

            debug("skew=%v.4 std=%v.4 buffer=%v", skewness, variance, buffer);
            cpsList.clear();
        }
        flying = 0;
    }
}
