package dev.brighten.anticheat.check.impl.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.processing.MovementProcessor;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Aim (E)", description = "Checks if a player's rotation was not calculated using Minecraft math.",
        checkType = CheckType.AIM,
        enabled = false, punishVL = 6)
public class AimE extends Check {

    private int buffer;
    @Packet
    public void process(WrappedInFlyingPacket packet) {
        if(!packet.isLook()) return;

        final double yawGcd = data.playerInfo.yawGCD,
                pitchGCD = data.playerInfo.pitchGCD;

        float modeToUse = Math.min(data.moveProcessor.yawMode, data.moveProcessor.pitchMode);
        boolean goodGcd = Math.max(data.moveProcessor.yawMode, data.moveProcessor.pitchMode)
                % modeToUse < 0.001;
        if((MathUtils.getDelta(data.moveProcessor.sensXPercent, data.moveProcessor.sensYPercent) < 1.5 || goodGcd)
                || (MathUtils.getDelta(data.moveProcessor.yawMode, yawGcd) > 0.1
                && MathUtils.getDelta(data.moveProcessor.pitchMode, pitchGCD) > 0.1)) {
            debug("sensitivity instability sx=%s sy=%s ym=%.2f pm=%.2f ygcd=%.2f pgcd=%.2f",
                    data.moveProcessor.sensXPercent, data.moveProcessor.sensYPercent, data.moveProcessor.yawMode,
                    data.moveProcessor.pitchMode, yawGcd, pitchGCD);
            return;
        }

        final float deltaYaw = Math.abs(data.playerInfo.deltaYaw), deltaPitch = Math.abs(data.playerInfo.deltaPitch);
        final double mx = (deltaYaw / modeToUse)
                % (Math.abs(data.playerInfo.lDeltaYaw) / modeToUse);
        final double my = (deltaPitch / modeToUse)
                % (Math.abs(data.playerInfo.lDeltaPitch) / modeToUse);

        final double deltaX = Math.abs(Math.floor(mx) - mx);
        final double deltaY = Math.abs(Math.floor(my) - my);

        final boolean shitX = deltaX > 0.08 && deltaX < 0.92, shitY = deltaY > 0.08 && deltaY < 0.92;
        final boolean increase = data.playerInfo.deltaYaw > modeToUse
                || data.playerInfo.deltaPitch > modeToUse;
        final boolean flag = (shitX && shitY) && (!increase || !data.playerInfo.cinematicMode);

        if(flag) {
            if(++buffer > 6) {
                vl++;
                flag("mx=%.2f my=%.2f dx=%.2f dy=%.2f", mx, my, deltaX, deltaY);
            }
        } else buffer = 0;

        debug((flag ? Color.Green + buffer + ": " : "") +"mx=%.2f my=%.2f dx=%.2f dy=%.2f s=%s inc=%s cin=%s",
                mx, my, deltaX, deltaY, data.moveProcessor.sensitivityX, increase, data.playerInfo.cinematicMode);
    }
}


