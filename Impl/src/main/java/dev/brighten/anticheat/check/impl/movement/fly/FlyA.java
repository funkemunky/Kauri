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

    private double maxHeight, groundY;
    private float slimeHeight;
    private boolean wasOnSlime, tookVelocity;

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        if(data.blockInfo.onSlime
                && data.playerInfo.lDeltaY < -.4) {
            wasOnSlime = true;
            slimeHeight = -1 * (float)data.playerInfo.lDeltaY;

            groundY = data.playerInfo.to.y;
            maxHeight = Math.max(maxHeight, MovementUtils.getTotalHeight(data.playerVersion, slimeHeight) * 2.5);
        } else if(!tookVelocity && data.playerInfo.serverGround) {
            groundY = data.playerInfo.to.y;
            wasOnSlime = false;
            maxHeight = data.playerInfo.totalHeight * 2.5;
        }

        if(data.playerInfo.lastVelocity.hasNotPassed(10)) {
            tookVelocity = true;
        } else if(data.playerInfo.serverGround) {
            tookVelocity = false;
        }

        if(tookVelocity || data.blockInfo.inLiquid || data.playerInfo.lastPlaceLiquid.hasNotPassed(10)
                || data.blockInfo.onClimbable) {
            groundY = data.playerInfo.to.y;
            maxHeight = data.playerInfo.totalHeight * 1.4;
        }

        if(data.playerInfo.clientGround || data.playerInfo.serverGround
                || data.playerInfo.flightCancel
                || data.playerInfo.serverPos
                || (data.playerInfo.blockOnTo != null && data.playerInfo.blockOnTo.getType().isSolid())
                || data.playerInfo.lastBlockPlace.hasNotPassed(7)) {
            groundY = data.playerInfo.to.y;
        }

        maxHeight = Math.max(1.3, maxHeight); //Fixes the occasional fuck up (usually on reload). Temporary.

        double totalHeight = data.playerInfo.to.y - groundY;
        if(totalHeight > maxHeight
                && packet.isPos()
                && !data.playerInfo.clientGround
                && !data.playerInfo.serverGround
                && !data.playerInfo.nearGround
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
