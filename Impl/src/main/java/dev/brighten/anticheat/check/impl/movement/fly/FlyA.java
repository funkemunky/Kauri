package dev.brighten.anticheat.check.impl.movement.fly;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.MovementUtils;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Fly (A)", description = "Sets a maximum height distance", punishVL = 5,
        checkType = CheckType.FLIGHT)
@Cancellable
public class FlyA extends Check {

    private double maxHeight, slimeHeight, totalHeight;
    private boolean wasOnSlime, tookVelocity;

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        if(packet.isPos()) {
            if(data.playerInfo.serverGround && data.blockInfo.onSlime && data.playerInfo.deltaY <= 0) {
                wasOnSlime = true;
                slimeHeight = 0;
                slimeHeight -= data.playerInfo.lDeltaY;
            } else if(wasOnSlime) {
                maxHeight = Math.max(2f,
                        MovementUtils.getTotalHeight((float)slimeHeight)) * 1.35f;
                wasOnSlime = false;
            } else if(!tookVelocity && data.playerInfo.serverGround) {
                maxHeight = MovementUtils.getTotalHeight(MovementUtils.getJumpHeight(data.getPlayer())) * 1.5f;
            }

            if(data.playerInfo.lastBlockPlace.hasNotPassed(5)) totalHeight = 0;

            if(data.playerInfo.lastToggleFlight.hasNotPassed(10)
                    || timeStamp - data.playerInfo.lastServerPos < 100L) {
                totalHeight = 0;
            }

            if(data.playerInfo.lastVelocity.hasNotPassed(10)) {
                tookVelocity = true;
            } else if(data.playerInfo.serverGround) {
                tookVelocity = false;
            }

            if(tookVelocity || data.blockInfo.inLiquid || data.blockInfo.onClimbable) {
                totalHeight = 0;
                maxHeight = MovementUtils.getTotalHeight((float)data.playerInfo.velocityY) * 1.4f;
            }

            if(data.playerInfo.clientGround || data.playerInfo.serverGround) {
                totalHeight = 0;
            } else if(data.playerInfo.deltaY > 0) totalHeight += data.playerInfo.deltaY;

            maxHeight = Math.max(1.3f, maxHeight); //Fixes the occasional fuck up (usually on reload). Temporary.

            if(totalHeight > maxHeight
                    && timeStamp - data.playerInfo.lastServerPos > 50L
                    && !data.playerInfo.serverPos
                    && !data.playerInfo.clientGround
                    && !data.playerInfo.serverGround
                    && !data.playerInfo.nearGround
                    && (!data.playerInfo.wasOnSlime || maxHeight >= 2)
                    && !data.playerInfo.flightCancel) {
                vl++;
                flag("%1>-%2; ping=%p tps=%t", totalHeight, maxHeight);
            }

            debug("total=" + totalHeight + " max=" + maxHeight
                    + " ground=" + data.playerInfo.serverGround
                    + " vel=" + tookVelocity + " slime=" + wasOnSlime);

            vl-= vl > 0 ? 0.01 : 0;
        }
    }
}
