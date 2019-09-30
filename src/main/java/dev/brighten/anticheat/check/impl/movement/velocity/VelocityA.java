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
    private long ticks;
    @Packet
    public void onVelocity(WrappedOutVelocityPacket packet) {
        if(packet.getId() == data.getPlayer().getEntityId() && packet.getY() > 0) {
            vY = (float) packet.getY();
        }
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        if(vY > 0
                && data.playerInfo.lastVelocity.hasNotPassed(4)
                && !data.playerInfo.generalCancel
                && !data.playerInfo.serverPos
                && !data.lagInfo.lagging
                && data.playerInfo.worldLoaded
                && !data.blockInfo.inWeb
                && !data.blockInfo.onClimbable
                && data.playerInfo.blocksAboveTicks == 0
                && !data.playerInfo.canFly) {

            float pct = data.playerInfo.deltaY / (float) vY * 100F;

            if (pct < 99.999 && !data.blockInfo.blocksAbove && !data.playerInfo.collidesHorizontally) {
                if (vl++ > 20) {
                    punish();
                } else if (vl > 4) flag("pct=" + MathUtils.round(pct, 2) + "%");
            } else vl-= vl > 0 ? 0.2 : 0;

            vY-= 0.08;
            vY*= 0.98;

            debug("pct=" + pct + " vl=" + vl);
        } else if(vY < 0) vY = 0;
    }
}
