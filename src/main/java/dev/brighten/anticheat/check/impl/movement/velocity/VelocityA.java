package dev.brighten.anticheat.check.impl.movement.velocity;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.CheckType;
import dev.brighten.anticheat.check.api.Packet;

@CheckInfo(name = "Velocity (A)", description = "Checks for vertical velocity modifications.",
        checkType = CheckType.VELOCITY, punishVL = 20)
public class VelocityA extends Check {

    private double vY;

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
                && !data.playerInfo.serverCanFly) {

            float pct = data.playerInfo.deltaY / (float) vY * 100F;

            if (pct < 99.999 && !data.blockInfo.blocksAbove && !data.playerInfo.collidesHorizontally) {
                if (vl++ > 9) flag("pct=" + MathUtils.round(pct, 2) + "%");
            } else vl-= vl > 0 ? 0.25 : 0;

            vY-= 0.08;
            vY*= 0.98;

            debug("pct=" + pct + " vl=" + vl);
        } else if(vY < 0) vY = 0;
    }
}
