package dev.brighten.anticheat.check.impl.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.CheckType;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.processing.MovementProcessor;
import dev.brighten.anticheat.utils.MiscUtils;

@CheckInfo(name = "Aim (D)", description = "Designed to detect aimassists attempting to use cinematic smoothing.",
        checkType = CheckType.AIM, punishVL = 10)
public class AimD extends Check {

    private long lastGCD;
    private boolean equal;
    private int yawTicks, pitchTicks, sprintTicks;
    private float lastPitch, lastYaw, verbose;

    @Packet
    public void onPacket(WrappedInFlyingPacket packet) {

        if(data.playerInfo.deltaYaw == 0) {
            yawTicks++;
        } else yawTicks = 0;

        if(data.playerInfo.deltaPitch == 0) {
            pitchTicks++;
        } else pitchTicks = 0;

        if(!data.playerInfo.sprinting) {
            sprintTicks++;
        } else sprintTicks = 0;

        if(packet.isLook()) {
            if(data.playerInfo.lastAttack.hasPassed(10) || sprintTicks > 4) {
                verbose = 0;
                return;
            }

            float pitchDelta = MathUtils.getDelta(data.playerInfo.to.pitch, lastPitch);
            long gcd = MiscUtils.gcd((long)(pitchDelta * MovementProcessor.offset),
                    (long)(lastPitch * MovementProcessor.offset));

            int resetInteger = yawTicks > 2 ? (pitchTicks * yawTicks) : yawTicks;

            if(resetInteger > 3 && verbose > 0) {
                verbose--;
            }

            if(data.playerInfo.cinematicModePitch && verbose > 0) {
                verbose-= verbose > 0 ? 4 : 0;
            }
            if(gcd == lastGCD && Math.abs(data.playerInfo.to.pitch) < 75) {
                verbose+=2;
            } else verbose-= verbose > 0 ? 2 : 0;

            debug("verbose=" + verbose + " gcd=" + gcd + " lGCD=" + lastGCD);
            if(data.playerInfo.deltaPitch == 0) {
                if(verbose > 10) {
                    vl++;
                    flag("verbose=" + verbose + " g=" + gcd);
                }
                lastYaw = data.playerInfo.to.yaw;
                lastPitch = data.playerInfo.to.pitch;
                lastGCD = gcd;
            }
        }
    }
}
