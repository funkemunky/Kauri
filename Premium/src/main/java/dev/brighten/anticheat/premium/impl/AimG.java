package dev.brighten.anticheat.premium.impl;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.processing.MovementProcessor;
import dev.brighten.api.KauriVersion;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Aim (G)", description = "Checks for bad GCD bypasses. (Rhys collab)",
        checkType = CheckType.AIM, punishVL = 30, developer = true, planVersion = KauriVersion.ARA)
public class AimG extends Check {

    private int buffer;
    @Packet
    public void process(WrappedInFlyingPacket packet) {
        if(!packet.isLook()) return;

        final double yawGcd = data.playerInfo.yawGCD / MovementProcessor.offset,
                pitchGCD = data.playerInfo.pitchGCD / MovementProcessor.offset;
        if(data.moveProcessor.sensYPercent != data.moveProcessor.sensXPercent
                || MathUtils.getDelta(data.moveProcessor.yawMode, yawGcd) > 0.1
                || MathUtils.getDelta(data.moveProcessor.pitchMode, pitchGCD) > 0.1) {
            debug("sensitivity instability sx=%v sy=%v ym=%v.2 pm=%v.2 ygcd=%v.2 pgcd=%v.2",
                    data.moveProcessor.sensXPercent, data.moveProcessor.sensYPercent, data.moveProcessor.yawMode,
                    data.moveProcessor.pitchMode, yawGcd, pitchGCD);
            return;
        }

        final float deltaYaw = Math.abs(data.playerInfo.deltaYaw), deltaPitch = Math.abs(data.playerInfo.deltaPitch);
        final double mx = (deltaYaw / data.moveProcessor.yawMode)
                % (Math.abs(data.playerInfo.lDeltaYaw) / data.moveProcessor.yawMode);
        final double my = (deltaPitch / data.moveProcessor.pitchMode)
                % (Math.abs(data.playerInfo.lDeltaPitch) / data.moveProcessor.pitchMode);

        final double deltaX = Math.abs(Math.floor(mx) - mx);
        final double deltaY = Math.abs(Math.floor(my) - my);

        final boolean shitX = deltaX > 0.05 && deltaX < 0.95, shitY = deltaY > 0.05 && deltaY < 0.95;
        final boolean flag = shitX && shitY;

        if(flag) {
            if(++buffer > 9) {
                vl++;
                flag("mx=%v.2 my=%v.2 dx=%v.2 dy=%v.2", mx, my, deltaX, deltaY);
            }
        } else if(buffer > 0) buffer-= 2;

        debug((flag ? Color.Green + buffer + ": " : "") +"mx=%v.2 my=%v.2 dx=%v.2 dy=%v.2 s=%v",
                mx, my, deltaX, deltaY, data.moveProcessor.sensitivityX);
    }

    private static float modulo(float s, float angle) {
        float f = (s * 0.6f + .2f);
        float f2 = f * f * f * 1.2f;
        return angle - (angle % f2);
    }
}