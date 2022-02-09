package dev.brighten.anticheat.check.impl.regular.movement.speed;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.anticheat.utils.MiscUtils;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Speed (B)", description = "Looks for direction changing in air, primarily AirStrafe.",
        checkType = CheckType.SPEED, punishVL = 8, vlToFlag = 2, executable = true)
@Cancellable
public class SpeedB extends Check {

    private double lastAngle;
    private float buffer;

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        //This gets the movement direction of the player. We want the absolute value to prevent false positives.
        //from 180 to -180, which doing a delta check would be 360, but really the angle difference is less than 1.
        double angle = Math.abs(MiscUtils.getAngle(data.playerInfo.to, data.playerInfo.from));

        detection: {
            //If player is on the ground or just came off the ground, we return
            if(data.playerInfo.clientGround || data.playerInfo.airTicks < 3) break detection;

            //These are situations other than ground which the player can change direction.
            //Timers are calculated in ticks; 1 = 50ms
            if(data.playerInfo.liquidTimer.isNotPassed(3)
                    || data.playerInfo.lastVelocity.isNotPassed(2)
                    || data.playerInfo.webTimer.isNotPassed(3))
                break detection;

            //Calculating the direction/angle change between last and current location
            double deltaAngle = Math.abs(angle - lastAngle);

            //1.5 is a guessed number based on past experience. Could be tighter or it could false flag.
            if(deltaAngle > 1.5f) {
                if(++buffer > 4) {
                    vl++;
                    flag("d=%.2f a=%.1f at=%s", deltaAngle, angle, data.playerInfo.airTicks);
                }
            } else if(buffer > 0) buffer-= 0.5f;

            debug("b=%.1f a=%.1f d=%.2f at=%s", buffer, angle, deltaAngle, data.playerInfo.airTicks);
        }
        lastAngle = angle;
    }
}