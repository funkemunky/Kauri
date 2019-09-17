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
        if(vY != 0 && ((data.playerInfo.deltaY > 0 && data.playerInfo.from.y % 0.5 == 0)
                || (System.currentTimeMillis() - velocityTS) > data.lagInfo.transPing * 2)
                && !data.playerInfo.generalCancel
                && data.playerInfo.blocksAboveTicks == 0
                && !data.playerInfo.canFly) {

            float pct = data.playerInfo.deltaY / (float) vY * 100F;

            if (pct < 99.999 && !data.blockInfo.blocksAbove && !data.playerInfo.collidesHorizontally) {
                if (vl++ > 20) {
                    punish();
                } else if (vl > 4) flag("pct=" + MathUtils.round(pct, 2) + "%");
            } else vl-= vl > 0 ? 0.5 : 0;
            vY = 0;

            debug("pct=" + pct + " vl=" + vl);
        }
    }
}
