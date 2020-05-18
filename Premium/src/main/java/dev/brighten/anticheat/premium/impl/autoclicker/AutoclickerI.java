package dev.brighten.anticheat.premium.impl.autoclicker;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.utils.Color;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Autoclicker (I)", description = "Checks to see if an autoclicker is on a low shift of data.",
        checkType = CheckType.AUTOCLICKER, developer = true)
public class AutoclickerI extends Check {

    private float buffer = 0;

    @Packet
    public void onClick(WrappedInArmAnimationPacket packet) {
        if (data.clickProcessor.isNotReady())
            return;

        double skewness = Math.abs(data.clickProcessor.getSkewness()),
                cpsMean = 1000L / data.clickProcessor.getMean(),
                variance = data.clickProcessor.getVariance();
        int outliers = data.clickProcessor.getOutliers();

        if(skewness < 0.1 && cpsMean > 8 && variance > 900) {
            if(++buffer > 9) {
                vl++;
                flag("skewness=%v.2 std=%v.2 var=%v.2", skewness, data.clickProcessor.getStd(), variance);
            }
            debug(Color.Green + "Flag");
        } else buffer-= buffer > 0 ? .5f : 0;

        debug("skewness=%v.3 std=%v.2 variance=%v.2 cpsMean=%v.2 outliers=%v modes=%v buffer=%v",
                skewness, data.clickProcessor.getStd(), variance, cpsMean,
                outliers, data.clickProcessor.getModes().size(), buffer);
    }
}
