package dev.brighten.anticheat.check.impl.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.timer.Timer;
import dev.brighten.anticheat.utils.timer.impl.TickTimer;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;

import java.util.List;

@CheckInfo(name = "Aim (B)", description = "Checks for common denominators in pitch difference.",
        checkType = CheckType.AIM, punishVL = 20, devStage = DevStage.RELEASE)
public class AimB extends Check {

    private float buffer;
    protected Timer lastGrid = new TickTimer(3);

    @Packet
    public void process(WrappedInFlyingPacket packet) {
        if (!packet.isLook()) return;

        float sensitivity = data.moveProcessor.sensitivityMcp;
        final float deltaYaw = Math.abs(data.playerInfo.deltaYaw),
                deltaPitch = Math.abs(data.playerInfo.deltaPitch);
        final float deltaX = deltaYaw / data.moveProcessor.yawMode,
                deltaY = deltaPitch / data.moveProcessor.pitchMode;

        if(data.moveProcessor.yawGcdList.size() < 40)
           return;

        final double gridX = getGrid(data.moveProcessor.yawGcdList),
                gridY = getGrid(data.moveProcessor.pitchGcdList);

        if (gridX < 0.005 || gridY < 0.005) lastGrid.reset();

        if (deltaX > 200
                || deltaY > 200) {
            debug("sensitivity instability: mcp=%.4f, cx=%.4f, cy=%.4f, dx=%.1f, dy=%.1f",
                    data.moveProcessor.sensitivityMcp, data.moveProcessor.currentSensX,
                    data.moveProcessor.currentSensY, deltaX, deltaY);
            if(buffer > 0) buffer--;
            return;
        }

        boolean increasing = deltaYaw > deltaX || deltaPitch > deltaY;
        aimB:
        {
            boolean flagged = false;
            if (data.playerInfo.pitchGCD < 0.007
                    && lastGrid.isPassed()
                    && data.playerInfo.lastHighRate.isNotPassed(3)) {
                if (deltaPitch < 10 && ++buffer > 8) {
                    vl++;
                    flag("%s", data.playerInfo.pitchGCD);
                }
                flagged = true;
            } else buffer = 0;

            debug((flagged ? Color.Green + buffer + ": " : "")
                            + "g=%s s=%s i=%s r=%s",
                    data.playerInfo.pitchGCD, data.moveProcessor.sensitivityMcp, increasing,
                    data.playerInfo.lastHighRate.isNotPassed() && lastGrid.isPassed());
        }
    }

    /*
     * This is an attempt to reverse the logistics of cinematic camera without having to run a full on prediction using
     * mouse filters. Otherwise, we would need to run more heavy calculations which is not really production friendly.
     * It may be more accurate but it is not really worth it if in the end of the day we're eating server performance.
     */
    protected static double getGrid(final List<Float> entry) {
        /*
         * We're creating the variables average min and max to start calculating the possibility of cinematic camera.
         * Why does this work? Cinematic camera is essentially a slowly increasing slowdown (which is why cinematic camera
         * becomes slower the more you use it) which in turn makes it so the min max and average are extremely close together.
         */
        double average = 0.0;
        double min = 0.0, max = 0.0;

        /*
         * These are simple min max calculations done manually for the sake of simplicity. We're using the numbers 0.0
         * since we also want to account for the possibility of a negative number. If there are no negative numbers then
         * there is absolutely no need for us to care about that number other than getting the max.
         */
        for (final double number : entry) {
            if (number < min) min = number;
            if (number > max) max = number;

            /*
             * Instead of having a sum variable we can use an average variable which we divide
             * right after the loop is over. Smart programming trick if you want to use it.
             */
            average += number;
        }

        /*
         * We're dividing the average by the length since this is the formula to getting the average.
         * Specifically its (sum(n) / length(n)) = average(n) -- with n being the entry set we're analyzing.
         */
        average /= entry.size();

        /*
         * This is going to estimate how close the average and the max were together with the possibility of a min
         * variable which is going to represent a negative variable since the preset variable on min is 0.0.
         */
        return (max - average) - min;
    }
}
