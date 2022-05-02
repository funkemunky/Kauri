package dev.brighten.anticheat.check.impl.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;

@CheckInfo(name = "Aim (H)", description = "checks for large headsnaps.",
        devStage = DevStage.BETA, checkType = CheckType.AIM, vlToFlag = 9)
public class AimH extends Check {
    private double lastHorizontalDistance;

    @Packet
    public void process(final WrappedInFlyingPacket packet, final long current) {
        final double horizontalDistance = data.playerInfo.deltaXZ;

        // Player moved
        if (packet.isPos() && !data.playerInfo.doingTeleport) {
            final float deltaYaw = Math.abs(data.playerInfo.deltaYaw);
            final float deltaPitch = Math.abs(data.playerInfo.deltaPitch);

            final boolean attacking = current - data.playerInfo.lastAttackTimeStamp < 100L;
            final double acceleration = Math.abs(horizontalDistance - lastHorizontalDistance);

            // Player made a large head rotation and didn't accelerate / decelerate which is impossible
            if (acceleration < 1e-02 && deltaYaw > 30.f && deltaPitch > 15.f && attacking) {
                vl++;
                flag("accel=%.2f deltaYaw=%.2f deltaPitch=%.2f attacking=%s",
                        acceleration, deltaYaw, deltaPitch, attacking);
            }
        }

        debug("looking=%s", data.playerInfo.lookingAtBlock);

        this.lastHorizontalDistance = horizontalDistance;
    }
}