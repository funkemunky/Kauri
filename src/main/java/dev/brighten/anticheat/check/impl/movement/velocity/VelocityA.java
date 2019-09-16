package dev.brighten.anticheat.check.impl.movement.velocity;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;

@CheckInfo(name = "VelocityA")
public class VelocityA extends Check {

    private float vY;
    private boolean moved;
    private int moveTicks;
    @Packet
    public void onVelocity(WrappedOutVelocityPacket packet) {
        if(packet.getId() == data.getPlayer().getEntityId()) vY = (float) packet.getY();
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(data.playerInfo.serverGround) {
            moved = false;
            if(moveTicks > 0) vY = 0;
        }

        if(data.playerInfo.deltaY > 0 && data.playerInfo.from.y % 0.5 == 0 && vY != 0
                && data.playerInfo.blocksAboveTicks == 0
                && !data.playerInfo.canFly) {
            moved = true;

            float pct = data.playerInfo.deltaY / vY * 100F;

            if(pct < 100) {
                if(vl++ > 20) {
                    //punish();
                }
            }

            debug("pct=" + pct);

            vY-= 0.08f;
            vY*= 0.98f;
            if(moveTicks++ > 4) {
                vY = 0;
                moveTicks = 0;
            }
        }
    }
}
