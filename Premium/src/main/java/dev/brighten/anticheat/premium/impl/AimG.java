package dev.brighten.anticheat.premium.impl;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutEntityHeadRotation;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.AtomicDouble;
import dev.brighten.anticheat.utils.Verbose;
import dev.brighten.api.check.CheckType;
import lombok.val;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.LongSummaryStatistics;

@CheckInfo(name = "Aim (G)", description = "A simple check to detect Vape's aimassist.",
        checkType = CheckType.AIM, developer = true, enabled = false, punishVL = 30)
public class AimG extends Check {

    private float yaw, pitch, lastYaw, lastPitch, lastDeltaYaw, lastDeltaPitch, lastYawDifference;
    private boolean sentRotation;
    private List<Float> yawSamples = new ArrayList<>(), pitchSamples = new ArrayList<>();

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(!packet.isLook()) return;

        // Get the yaw/pitch values from the rotation
        final float yaw = packet.getYaw();
        final float pitch = packet.getPitch();

        // Get the yaw/pitch values from the outGoing rotation
        final float outYaw = this.yaw;
        final float outPitch = this.pitch;

        // Getting the delta of the last yaw/pitch values and the current outGoing values
        final float deltaYaw = MathUtils.getAngleDelta(this.lastYaw, outYaw);
        final float deltaPitch = MathUtils.getAngleDelta(this.lastPitch, outPitch);

        // Get the difference between the previous rotations
        final float differenceYaw = Math.abs(lastDeltaYaw - deltaYaw);
        final float differencePitch = Math.abs(lastDeltaPitch - deltaPitch);

        // Get the Jolt of the rotations
        final float joltYaw = Math.abs(differenceYaw - lastYawDifference);
        final float joltPitch = Math.abs(differencePitch - lastYawDifference);

        // I abused jeremy
        if (differenceYaw > 2.0 && differencePitch == 0.0) {
            yawSamples.add(joltYaw);
            pitchSamples.add(joltPitch);

            if (yawSamples.size() == 20 && pitchSamples.size() == 20) {
                final long distinctYaw = yawSamples.stream().distinct().count();
                final long distinctPitch = pitchSamples.stream().distinct().count();

                final long duplicatesYaw = yawSamples.size() - distinctYaw;
                final long duplicatesPitch = pitchSamples.size() - distinctPitch;

                final boolean invalid = (duplicatesYaw == 0.0 || duplicatesPitch == 0.0) && duplicatesYaw < 2 && duplicatesPitch < 2;

                if (invalid) {
                    vl++;
                    flag("dup=" + duplicatesYaw + " dupp=" + duplicatesPitch);
                } else debug("nope");

                yawSamples.clear();
                pitchSamples.clear();
            }
        }

        this.lastYaw = yaw;
        this.lastPitch = pitch;
        this.lastDeltaYaw = deltaYaw;
        this.lastDeltaPitch = deltaPitch;
        this.lastYawDifference = differenceYaw;
    }

    @Packet
    public void onRotation(WrappedOutEntityHeadRotation packet) {
        yaw = packet.getPlayer().getLocation().getYaw();
        pitch = packet.getPlayer().getLocation().getPitch();
        sentRotation = true;
    }
}
