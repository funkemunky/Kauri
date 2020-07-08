package dev.brighten.anticheat.premium.impl;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import com.google.common.collect.Lists;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.processing.MovementProcessor;
import dev.brighten.anticheat.utils.MiscUtils;
import dev.brighten.api.check.CheckType;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

@CheckInfo(name = "Aim (G)", description = "Checks if the yaw rotation snaps.",
        checkType = CheckType.AIM, punishVL = 10)
public class AimG extends Check {

    private final Deque<Float> samplesYaw = Lists.newLinkedList();
    private final Deque<Float> samplesPitch = Lists.newLinkedList();

    private double buffer = 0.0;
    private double lastAverage = 0.0;

    @Packet
    public void process(WrappedInFlyingPacket packet, long now) {
        if(!packet.isLook()) return;

        final float deltaYaw = Math.abs(data.playerInfo.deltaYaw);
        final float deltaPitch = Math.abs(data.playerInfo.deltaPitch);

        final boolean attacking = now - data.playerInfo.lastAttackTimeStamp < 500L;

        if (deltaYaw > 0.0 && deltaPitch > 0.0 && attacking) {
            samplesYaw.add(deltaYaw);
            samplesPitch.add(deltaPitch);
        }

        if (samplesPitch.size() == 16 && samplesYaw.size() == 16) {
            final double averageYaw = samplesYaw.stream().mapToDouble(d -> d).average().orElse(0.0);
            final double averagePitch = samplesPitch.stream().mapToDouble(d -> d).average().orElse(0.0);

            final double deviation = MiscUtils.stdev(samplesPitch);
            final double averageDelta = Math.abs(averagePitch - lastAverage);

            if (deviation > 6.f && averageDelta > 1f && averageYaw < 30.d) {
                if (++buffer > 4) {
                    vl++;
                    flag("");
                }
            } else {
                buffer = Math.max(buffer - 0.125, 0);
            }

            debug("buffer=%v.2 deviaion=%v.3 avgDelta=%v.3 averageYaw=%v.3",
                    buffer, deviation, averageDelta, averageYaw);
            samplesYaw.clear();
            samplesPitch.clear();
            lastAverage = averagePitch;
        }
    }
}