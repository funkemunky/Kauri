package dev.brighten.anticheat.check.impl.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.processing.MovementProcessor;
import dev.brighten.anticheat.utils.MiscUtils;
import dev.brighten.api.check.CheckType;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

@CheckInfo(name = "Aim (L)", description = "Elevated's rotation check thing.", checkType = CheckType.AIM)
public class AimL extends Check {

    private float lastYaw, lastPitch, lastOutYawDelta, lastOutPitchDelta, lastPitchDelta, lastYawDelta;
    private Deque<Long> divisorDeque = new LinkedList<>();

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if (packet.isLook()) {
            float yaw = packet.getYaw();
            float pitch = packet.getPitch();

            float yawDelta = Math.abs(this.lastYaw - yaw);
            float pitchDelta = Math.abs(this.lastPitch - pitch);

            float outYaw = data.playerInfo.headYaw;
            float outPitch = data.playerInfo.headPitch;

            float outYawDelta = MathUtils.getAngleDelta(this.lastYaw, outYaw);
            float outPitchDelta = MathUtils.getAngleDelta(this.lastPitch, outPitch);

            float outYawDifference = MathUtils.getAngleDelta(outYawDelta, this.lastOutYawDelta);

            if (outYawDifference > 2.0F && yawDelta > 0.0F) {
                long expandedOutPitch = (long) (outPitchDelta * MovementProcessor.offset);
                long previousExpandedOutPitch = (long) (lastOutPitchDelta * MovementProcessor.offset);

                long expandedPitch = (long) (pitchDelta * MovementProcessor.offset);
                long previousExpandedPitch = (long) (lastPitchDelta * MovementProcessor.offset);

                long pitchOutDivisor = MiscUtils.gcd(expandedOutPitch, previousExpandedOutPitch);
                long pitchDivisor = MiscUtils.gcd(expandedPitch, previousExpandedPitch);

                long divisorDiff = Math.abs(pitchOutDivisor - pitchDivisor);

                this.divisorDeque.addLast(divisorDiff);

                if (this.divisorDeque.size() == 20) {

                    AtomicInteger level = new AtomicInteger();

                    this.divisorDeque.stream().filter(div -> div == 0.0).forEach(div -> level.incrementAndGet());

                    if (level.get() > 10 && data.playerInfo.lastAttack.hasNotPassed(20)) {
                        vl++;
                        flag("level=%1", level.get());
                    }

                    debug("level=%1", level.get());

                    this.divisorDeque.clear();
                }
            }

            this.lastOutYawDelta = outYawDelta;
            this.lastOutPitchDelta = outPitchDelta;
            this.lastYaw = yaw;
            this.lastPitch = pitch;
            this.lastPitchDelta = pitchDelta;
        }
    }
}
