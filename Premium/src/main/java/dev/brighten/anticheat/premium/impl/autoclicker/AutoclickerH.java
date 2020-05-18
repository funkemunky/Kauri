package dev.brighten.anticheat.premium.impl.autoclicker;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.GraphUtil;
import dev.brighten.api.check.CheckType;
import lombok.val;

import java.util.Deque;
import java.util.LinkedList;

@CheckInfo(name = "Autoclicker (H)", description = "Meant to detect Vape and other autoclickers. By Elevated.",
        checkType = CheckType.AUTOCLICKER, punishVL = 7, vlToFlag = 2)
public class AutoclickerH extends Check {

    private double ticks, cps, buffer;
    private double lastKurt;
    private Deque<Double> clickSamples = new LinkedList<>();

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if (++ticks == 20) {
            if (cps > 8 && clickSamples.add(cps) && clickSamples.size() == 10) {
                final GraphUtil.GraphResult results = GraphUtil.getGraph(clickSamples);

                val negatives = results.getNegatives();
                val kurtosis = data.clickProcessor.getKurtosis();
                if (negatives == 1 && MathUtils.getDelta(kurtosis, lastKurt) > 0.2) {
                    if (++buffer > 3) {
                        vl++;
                        flag("cps=%v buffer=%v", cps, buffer);
                    }
                } else {
                    buffer = 0;
                }
                this.clickSamples.clear();
                debug("cps=%v negatives=%v buffer=%v kurt=%v.2",
                        cps, negatives, MathUtils.round(buffer, 1), kurtosis);
                lastKurt = kurtosis;
            }
            this.cps = 0;
            this.ticks = 0;
        }
    }

    @Packet
    public void onArm(WrappedInArmAnimationPacket packet) {
        if(data.playerInfo.breakingBlock
                || data.playerInfo.lastBlockPlace.hasNotPassed(2)) return;
        ++cps;
    }
}
