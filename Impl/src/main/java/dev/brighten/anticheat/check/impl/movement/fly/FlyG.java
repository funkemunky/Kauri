package dev.brighten.anticheat.check.impl.movement.fly;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Fly (G)", description = "Checks if the player stops abruptly without reason.",
        checkType = CheckType.FLIGHT, developer = true, enabled = false)
@Cancellable
public class FlyG extends Check {

    private double totalY;
    private boolean cancelled;
    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        if(data.playerInfo.generalCancel || data.playerInfo.onLadder) {
            cancelled = true;
            totalY = 0;
            return;
        }
        if(data.playerInfo.clientGround || data.playerInfo.deltaY <= 0) {
            if(totalY > 0) {
                if(data.playerInfo.blockAboveTimer.hasPassed(5)
                        && !data.blockInfo.onStairs
                        && !cancelled
                        && totalY > 0.1
                        && !data.playerInfo.wasOnSlime
                        && data.playerInfo.slimeTimer.hasPassed(10)
                        && data.playerInfo.webTimer.hasPassed(10)
                        && data.playerInfo.liquidTimer.hasPassed(30)
                        && data.playerInfo.climbTimer.hasPassed(10)
                        && data.playerInfo.lastBlockPlace.hasPassed(20)
                        && (data.playerInfo.lastHalfBlock.hasPassed(7) || totalY > 1)
                        && totalY > 0.5
                        && data.playerInfo.lastVelocity.hasPassed(20)) {
                    double delta = MathUtils.getDelta(totalY, data.playerInfo.totalHeight);

                    if(delta >= (data.playerInfo.lastHalfBlock.hasNotPassed(10)
                            || data.playerInfo.lastInsideBlock.hasNotPassed(20)
                            || data.blockInfo.blocksNear
                            ? 0.02 : 1E-7) + (data.playerVersion.isOrAbove(ProtocolVersion.V1_9) ? 0.005 : 0)) {
                        vl++;
                        flag("delta=%1 totalHeight=%2 predicted=%3 jumpHeight=%4",
                                MathUtils.round(delta, 3),
                                MathUtils.round(totalY, 3),
                                MathUtils.round(data.playerInfo.totalHeight, 3),
                                MathUtils.round(data.playerInfo.jumpHeight, 3));
                    }

                    debug("delta=%1", delta);
                }
            }
            totalY = 0;
        } else if(data.playerInfo.deltaY > 0 && !cancelled) totalY+= data.playerInfo.deltaY;
        if(data.playerInfo.clientGround) cancelled = false;
    }
}
