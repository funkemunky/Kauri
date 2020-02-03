package dev.brighten.anticheat.check.impl.movement.fly;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Fly (G)", description = "Checks if the player stops abruptly without reason.",
        checkType = CheckType.FLIGHT, developer = true)
public class FlyG extends Check {

    private double totalY;
    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        if(data.playerInfo.flightCancel) {
            totalY = 0;
            return;
        }
        if(data.playerInfo.serverGround || data.playerInfo.deltaY <= 0) {
            if(totalY > 0) {
                if(data.playerInfo.blockAboveTimer.hasPassed(5)
                        && data.playerInfo.lastVelocity.hasPassed(20)) {
                    double delta = MathUtils.getDelta(totalY, data.playerInfo.totalHeight);

                    if(delta >= 1E-7) {
                        vl++;
                        flag("delta=%1 predicted=%2 jumpHeight=%3", delta,
                                MathUtils.round(data.playerInfo.totalHeight, 3),
                                MathUtils.round(data.playerInfo.jumpHeight, 3));
                    }

                    debug("delta=%1", delta);
                }
            }
            totalY = 0;
        } else totalY+= data.playerInfo.deltaY;
    }
}
