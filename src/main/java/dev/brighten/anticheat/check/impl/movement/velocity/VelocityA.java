package dev.brighten.anticheat.check.impl.movement.velocity;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;

@CheckInfo(name = "Velocity (A)", description = "Checks for vertical velocity modifications.")
public class VelocityA extends Check {

    private double vY;
    private boolean moved;
    private int moveTicks;
    private long velocityTS;
    @Packet
    public void onVelocity(WrappedOutVelocityPacket packet) {
        if(packet.getId() == data.getPlayer().getEntityId()) {
            vY = (float) packet.getY();
            velocityTS = System.currentTimeMillis();
        }
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(data.playerInfo.serverGround && moveTicks > 0) {
            vY = moveTicks = 0;
        }

        if((data.playerInfo.deltaY > 0 && data.playerInfo.from.y % 0.5 == 0
                || moveTicks > 0
                || (System.currentTimeMillis() - velocityTS) > data.lagInfo.transPing * 2)
                && vY != 0
                && !data.playerInfo.generalCancel
                && data.playerInfo.blocksAboveTicks == 0
                && !data.playerInfo.canFly) {

            float pct = data.playerInfo.deltaY / (float)vY * 100F;

            if(pct < 99.999) {
                if(vl++ > 20) {
                    punish();
                } else if(vl > 4) flag("pct=" + MathUtils.round(pct, 2) + "%");
            }

            debug("pct=" + pct);

            vY-= 0.08;
            vY*= 0.9800000190734863D;
            if(moveTicks++ > 4) {
                vY = 0;
                moveTicks = 0;
            }
        }
    }
}
