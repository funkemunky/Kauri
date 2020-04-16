package dev.brighten.anticheat.check.impl.movement.velocity;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInKeepAlivePacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInTransactionPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Velocity (A)", description = "Checks for vertical velocity modifications.",
        checkType = CheckType.VELOCITY, punishVL = 25)
@Cancellable
public class VelocityA extends Check {

    private double vY, tvY;
    private long velocityTS;
    private boolean tookVelocity;

    @Packet
    public void onVelocity(WrappedOutVelocityPacket packet, long timeStamp) {
        if(packet.getId() == data.getPlayer().getEntityId()) {
            tvY = packet.getY();
            velocityTS = timeStamp;
            tookVelocity = true;
        }
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        if(tookVelocity && !data.playerInfo.clientGround
                && data.playerInfo.lClientGround) {
            tookVelocity = false;
            vY = tvY;
        }
        if(vY > 0
                && !data.playerInfo.generalCancel
                && !data.lagInfo.lagging
                && data.playerInfo.worldLoaded
                && !tookVelocity
                && !data.blockInfo.inWeb
                && data.lagInfo.lastPacketDrop.hasPassed(5)
                && !data.blockInfo.onClimbable
                && data.playerInfo.blockAboveTimer.hasPassed(6)) {

            double pct = data.playerInfo.deltaY / vY * 100;

            if (pct < 99.999
                    && !data.playerInfo.lastBlockPlace.hasNotPassed(5)
                    && !data.blockInfo.blocksAbove) {
                if (++vl > 15) flag("pct=" + MathUtils.round(pct, 2) + "%");
            } else vl-= vl > 0 ? 0.25f : 0;

            vY-= 0.08;
            vY*= 0.98;

            if(vY < 0.005
                    || (timeStamp - velocityTS) > 400L
                    || data.blockInfo.collidesHorizontally
                    || data.blockInfo.collidesVertically) vY = 0;

            debug("pct=" + pct + " vl=" + vl);
        } else vY = 0;
    }
}
