package dev.brighten.anticheat.check.impl.regular.movement.velocity;

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
        checkType = CheckType.VELOCITY, punishVL = 25)
@Cancellable
public class VelocityA extends Check {

    private double vY, cvY, buffer;
    private TickTimer lastVelocity = new TickTimer(20);

    @Packet
    public void onVelocity(WrappedOutVelocityPacket packet) {
        if(packet.getId() == data.getPlayer().getEntityId() && packet.getY() > 0.1) {
           cvY = packet.getY();
        }
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        //Make sure we don't have any false positives because of a lingering velocity check
        if(cvY > 0 && vY == 0 && data.playerInfo.lastVelocity.isPassed(3 + data.lagInfo.transPing)) cvY = 0;

        //Player just jumped into the air
        if(!data.playerInfo.clientGround && data.playerInfo.lClientGround
                && data.playerInfo.from.y % 1. == 0
                && data.playerInfo.deltaY > 0)
            vY = cvY;

        //If any of these conditions aren't met, we don't want to make checking for vertical velocity.
        if(vY > 0
                && !data.playerInfo.generalCancel
                && data.playerInfo.worldLoaded
                && data.playerInfo.lastTeleportTimer.isPassed(2)
                && !data.blockInfo.inWeb
                && !data.blockInfo.onClimbable
                && data.playerInfo.blockAboveTimer.isPassed(6)) {

            double pct = data.playerInfo.deltaY / vY * 100;

            if ((pct < 99.999 || pct > 300)
                    && !data.playerInfo.lastBlockPlace.isNotPassed(5)
                    && !data.blockInfo.blocksAbove) {
                if(++buffer > 15) {
                    vl++;
                    flag("pct=%.1f%% buffer=%.1f", pct, buffer);
                }
                if (++vl > 15) flag("pct=" + MathUtils.round(pct, 2) + "%%");
            } else buffer-= buffer > 0 ? 0.25f : 0;

            vY-= 0.08;
            vY*= 0.98;

            //If any of these conditions are met, we might as well stop checking for velocity.
            if(vY < 0.005 //While this is a 1.8.9 and below only thing, we might as well stop checking.
                    || data.playerInfo.lastVelocity.isPassed(7)
                    || data.blockInfo.collidesHorizontally
                    || data.blockInfo.collidesVertically) vY = 0;


            debug("pct=" + pct + " vl=" + vl);
        } else vY = 0;
    }
}