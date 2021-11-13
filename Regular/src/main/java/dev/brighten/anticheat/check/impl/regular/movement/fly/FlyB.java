package dev.brighten.anticheat.check.impl.regular.movement.fly;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.MovementUtils;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;

@CheckInfo(name = "Fly (B)", description = "Looks for players going above a possible height limit",
        checkType = CheckType.FLIGHT, devStage = DevStage.CANARY, vlToFlag = 4, punishVL = 15)
@Cancellable
public class FlyB extends Check {

    private double vertical, limit, velocityY, slimeY;

    @Packet
    public void onVelocity(WrappedOutVelocityPacket packet) {
        if(packet.getId() == data.getPlayer().getEntityId()) {
            velocityY = MovementUtils.getTotalHeight(data.playerVersion, (float)packet.getY());
        }
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if (data.playerInfo.generalCancel
                || data.playerInfo.liquidTimer.isNotPassed(2)
                || data.playerInfo.canFly
                || data.playerInfo.creative
                || data.playerInfo.climbTimer.isNotPassed(2)) {
            vertical = 0;
            limit = Double.MAX_VALUE;
            return;
        }
        if(data.playerInfo.serverGround) {
            vertical = 0;

            limit = MovementUtils.getTotalHeight(data.playerVersion, MovementUtils.getJumpHeight(data));
            if(data.playerInfo.lastVelocity.isPassed(3)) velocityY = 0;

            if(data.playerInfo.wasOnSlime && data.playerInfo.clientGround) {
                slimeY = MovementUtils.getTotalHeight(data.playerVersion, (float)Math.abs(data.playerInfo.deltaY));
                debug("SLIME: sy=%.2f", slimeY);
            } else if(data.playerInfo.slimeTimer.isPassed(4)) slimeY = 0;
        } else {
            vertical += data.playerInfo.deltaY;

            double limit = (this.limit + slimeY + velocityY) * 1.25;

            if(vertical > limit) {
                vl++;
                flag("%.3f>-%.3f", vertical, limit);
            }

            debug("v=%.3f l=%.3f", vertical, limit);
        }
    }
}