package dev.brighten.anticheat.check.impl.movement.velocity;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.TickTimer;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Velocity (A)", description = "Checks for vertical velocity modifications.",
        checkType = CheckType.VELOCITY, punishVL = 15, executable = true)
@Cancellable
public class VelocityA extends Check {

    private double vY, buffer;

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {

        //Player just jumped into the air
        if(data.playerInfo.cva) {
            if(data.playerInfo.velocityY > 0.1)
                vY = data.playerInfo.velocityY;
            data.playerInfo.cva = false;
        }  else if(data.playerInfo.doingBlockUpdate) {
            vY = 0;
            return;
        }

        //If any of these conditions aren't met, we don't want to make checking for vertical velocity.
        if(vY > 0
                && !data.playerInfo.generalCancel
                && data.playerInfo.worldLoaded
                && data.playerInfo.lastTeleportTimer.isPassed(2)
                && !data.blockInfo.inWeb
                && !data.blockInfo.onClimbable
                && data.playerInfo.blockAboveTimer.isPassed(6)) {

            double pct = data.playerInfo.deltaY / vY * 100;

            if ((pct < 99.999 || pct > 400)
                    && !data.playerInfo.lastBlockPlace.isNotPassed(5)
                    && !data.blockInfo.blocksAbove) {
                if(++buffer > 15) {
                    vl++;
                    flag("pct=%.1f%% buffer=%.1f", pct, buffer);
                }
            } else buffer-= buffer > 0 ? 0.5 : 0;

            vY-= 0.08;
            vY*= 0.98;

            //If any of these conditions are met, we might as well stop checking for velocity.
            if(vY < 0.005 //While this is a 1.8.9 and below only thing, we might as well stop checking.
                    || data.playerInfo.lastVelocity.isPassed(7)
                    || data.blockInfo.collidesHorizontally
                    || data.blockInfo.collidesVertically) vY = 0;

            debug("pct=" + pct + " vl=" + buffer);
        }
    }
}