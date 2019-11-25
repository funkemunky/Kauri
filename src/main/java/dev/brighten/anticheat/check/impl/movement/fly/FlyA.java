package dev.brighten.anticheat.check.impl.movement.fly;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.CheckType;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.MovementUtils;

@CheckInfo(name = "Fly (A)", description = "Sets a maximum height distance", punishVL = 5,
        checkType = CheckType.FLIGHT)
public class FlyA extends Check {

    private float maxHeight, slimeHeight, totalHeight;
    private boolean wasOnSlime, tookVelocity;

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(packet.isPos()) {
            if(data.playerInfo.wasOnSlime && data.playerInfo.deltaY < 0) {
                wasOnSlime = true;
                slimeHeight = -data.playerInfo.deltaY;
            } if(wasOnSlime && data.playerInfo.deltaY > 0) {
                maxHeight = MovementUtils.getTotalHeight(data.getPlayer(), slimeHeight);
                wasOnSlime = false;
            } else if(!tookVelocity && data.playerInfo.serverGround) {
                maxHeight = MovementUtils.getTotalHeight(
                        data.getPlayer(),
                        MovementUtils.getJumpHeight(data.getPlayer()));
            }

            if(data.playerInfo.lastVelocity.hasNotPassed(10)) {
                tookVelocity = true;
            } else if(data.playerInfo.serverGround) {
                tookVelocity = false;
            }

            if(tookVelocity) {
                maxHeight = MovementUtils.getTotalHeight(data.getPlayer(), data.playerInfo.velocityY);
            }

            maxHeight+= 0.1f;

            if(data.playerInfo.serverGround) {
                totalHeight = 0;
            } else if(data.playerInfo.deltaY > 0) totalHeight += data.playerInfo.deltaY;

            if(totalHeight > maxHeight
                    && !data.blockInfo.inLiquid && !data.blockInfo.inWeb
                    && !data.playerInfo.canFly && data.playerInfo.lastToggleFlight.hasPassed(20)) {
                vl++;
                flag(totalHeight + ">-" + maxHeight);
            }

            debug("total=" + totalHeight + " max=" + maxHeight
                    + " ground=" + data.playerInfo.serverGround
                    + " vel=" + tookVelocity + " slime=" + wasOnSlime);

            vl-= vl > 0 ? 0.01 : 0;
        }
    }
}
