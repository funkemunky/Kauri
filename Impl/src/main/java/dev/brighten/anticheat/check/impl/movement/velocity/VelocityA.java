package dev.brighten.anticheat.check.impl.movement.velocity;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInTransactionPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Velocity (A)", description = "Checks for vertical velocity modifications.",
        checkType = CheckType.VELOCITY, punishVL = 20, developer = true, executable = false)
public class VelocityA extends Check {

    private double vY;
    private long velocityTS;

    @Packet
    public void onVelocity(WrappedOutVelocityPacket packet) {
        if(packet.getId() == data.getPlayer().getEntityId() && packet.getY() > 0) {
            vY = (float) packet.getY();
        }
    }
    
    @Packet
    public void onTransaction(WrappedInTransactionPacket packet, long timeStamp) {
        if(packet.getAction() == (short) 101) {
            velocityTS = timeStamp;
        }
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        if(vY > 0
                && (timeStamp - velocityTS) < 200
                && !data.playerInfo.generalCancel
                && !data.playerInfo.serverPos
                && !data.lagInfo.lagging
                && data.playerInfo.worldLoaded
                && !data.blockInfo.inWeb
                && !data.blockInfo.onClimbable
                && data.playerInfo.blocksAboveTicks.value() == 0) {

            float pct = Math.max(0, data.playerInfo.lastVelocityTimestamp);

            if (pct < 99.999
                    && !data.playerInfo.lastBlockPlace.hasNotPassed(5)
                    && !data.blockInfo.blocksAbove
                    && !data.blockInfo.collidesHorizontally) {
                if (vl++ > 9) flag("pct=" + MathUtils.round(pct, 2) + "%");
            } else vl-= vl > 0 ? 0.5 : 0;

            vY-= 0.08;
            vY*= 0.98;

            debug("pct=" + pct + " vl=" + vl);
        } else if(vY < 0) vY = 0;
    }
}
