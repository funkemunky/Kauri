package dev.brighten.anticheat.check.impl.regular.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.TickTimer;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.processing.MovementProcessor;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Aim (E)", description = "Checks if a player's rotation was not calculated using Minecraft math.",
        checkType = CheckType.AIM, punishVL = 15, executable = true)
public class AimE extends Check {

    private int buffer;

    @Packet
    public void process(WrappedInFlyingPacket packet) {
        if(!packet.isLook()) return;

        float sensitivity = data.moveProcessor.sensitivityMcp;
        final float deltaYaw = Math.abs(data.playerInfo.deltaYaw),
                deltaPitch = Math.abs(data.playerInfo.deltaPitch);
        final float deltaX = deltaYaw / data.moveProcessor.yawMode,
                deltaY = deltaPitch / data.moveProcessor.pitchMode;

        if(data.moveProcessor.yawGcdList.size() < 40
                || MathUtils.getDelta(sensitivity, data.moveProcessor.currentSensY) > 0.8
                || MathUtils.getDelta(sensitivity, data.moveProcessor.currentSensX) > 0.8
                || deltaX > 200
                || deltaY > 200) {
            debug("sensitivity instability: mcp=%.4f, cx=%.4f, cy=%.4f, dx=%.1f, dy=%.1f",
                    data.moveProcessor.sensitivityMcp, data.moveProcessor.currentSensX,
                    data.moveProcessor.currentSensY, deltaX, deltaY);
            return;
        }
        final double mx = (deltaYaw / sensitivity) % (Math.abs(data.playerInfo.lDeltaYaw) / sensitivity),
                my = (deltaPitch / sensitivity)
                        % (Math.abs(data.playerInfo.lDeltaPitch) / sensitivity);

        final double dmx = Math.abs(Math.floor(mx) - mx),
                dmy = Math.abs(Math.floor(my) - my);

        final boolean shitX = dmx > 0.08 && dmx < 0.92, shitY = dmy > 0.08 && dmy < 0.92,
                increase = deltaYaw > deltaX || deltaPitch > deltaY,
                flag = (shitX && shitY) && !increase && data.playerInfo.lastHighRate.isPassed();

        if(flag) {
            if(++buffer > 6) {
                vl++;
                flag("mx=%.2f my=%.2f dx=%.2f dy=%.2f", mx, my, dmx, dmy);
            }
        } else buffer = 0;

        debug((flag ? Color.Green + buffer + ": " : "") +"mx=%.2f my=%.2f dx=%.2f dy=%.2f s=%s inc=%s",
                mx, my, dmx, dmy, data.moveProcessor.sensitivityMcp, increase);
    }
}


