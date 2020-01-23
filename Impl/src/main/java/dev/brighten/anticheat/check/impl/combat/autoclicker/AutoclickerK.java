package dev.brighten.anticheat.check.impl.combat.autoclicker;

import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockDigPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.api.check.CheckType;
import lombok.val;

import java.util.Deque;
import java.util.LinkedList;

@CheckInfo(name = "Autoclicker (K)", description = "STD Autoclicker check (fast)", checkType = CheckType.AUTOCLICKER)
public class AutoclickerK extends Check {

    private Deque<Long> clickSamples = new LinkedList<>();

    private long lastSwing;

    @Packet
    public void onClick(WrappedInArmAnimationPacket packet, long timeStamp) {
        long now = System.currentTimeMillis();
        long delay = now - this.lastSwing;

        if (data.playerInfo.lastBrokenBlock.hasNotPassed(3) || data.playerInfo.lastBlockPlace.hasNotPassed(1))
            return;

        if (delay > 1L && delay < 300L && this.clickSamples.add(delay) && this.clickSamples.size() == 30) {
            double average = this.clickSamples.stream().mapToDouble(Long::doubleValue).average().orElse(0.0);

            double stdDeviation = 0.0;

            for (Long click : this.clickSamples) {
                stdDeviation += Math.pow(click.doubleValue() - average, 2);
            }

            stdDeviation /= this.clickSamples.size();

            val std = Math.sqrt(stdDeviation);
            if (std < 30.d) {
                vl++;
                this.flag("STD: " + stdDeviation);
            }

            debug("std=" + std + " std2=" + stdDeviation);

            this.clickSamples.clear();
        }

        this.lastSwing = now;
    }
}
