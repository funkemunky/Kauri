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
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        if(packet.isPos()) {
            if(data.playerInfo.clientGround && data.blockInfo.onSlime && data.playerInfo.deltaY <= 0) {
                wasOnSlime = true;
                slimeHeight = 0;
                slimeHeight -= data.playerInfo.lDeltaY;
            } else if(wasOnSlime) {
                maxHeight = Math.max(2f,
                        MovementUtils.getTotalHeight(data.getPlayer(), slimeHeight)) * 1.25f;
                wasOnSlime = false;
            } else if(!tookVelocity && data.playerInfo.serverGround) {
                maxHeight = MovementUtils.getTotalHeight(
                        data.getPlayer(),
                        MovementUtils.getJumpHeight(data.getPlayer())) + 0.1f;
            }

            if(data.playerInfo.lastToggleFlight.hasNotPassed(10) || timeStamp - data.playerInfo.lastServerPos < 100L) {
                totalHeight = 0;
            }

            if(data.playerInfo.lastVelocity.hasNotPassed(10)) {
                tookVelocity = true;
            } else if(data.playerInfo.serverGround) {
                tookVelocity = false;
            }

            if(tookVelocity) {
                totalHeight = 0;
                maxHeight = MovementUtils.getTotalHeight(data.getPlayer(), data.playerInfo.velocityY) + 0.1f;
            }

            if(data.playerInfo.clientGround) {
                totalHeight = 0;
            } else if(data.playerInfo.deltaY > 0) totalHeight += data.playerInfo.deltaY;

            if(totalHeight > maxHeight
                    && timeStamp - data.playerInfo.lastServerPos > 50L
                    && !data.playerInfo.serverPos
                    && !data.playerInfo.clientGround
                    && (!data.playerInfo.wasOnSlime || maxHeight >= 2)
                    && !data.playerInfo.flightCancel) {
                vl++;
                if(vl > 1) {
                    flag(totalHeight + ">-" + maxHeight);
                }
            }

            debug("total=" + totalHeight + " max=" + maxHeight
                    + " ground=" + data.playerInfo.serverGround
                    + " vel=" + tookVelocity + " slime=" + wasOnSlime);

            vl-= vl > 0 ? 0.01 : 0;
        }
    }
}
