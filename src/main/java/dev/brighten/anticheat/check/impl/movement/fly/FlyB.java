package dev.brighten.anticheat.check.impl.movement.fly;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.CheckType;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.MovementUtils;

@CheckInfo(name = "Fly (B)", description = "Checks for improper acceleration.", checkType = CheckType.FLIGHT)
public class FlyB extends Check {

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(packet.isPos() && (data.playerInfo.deltaY != 0 || data.playerInfo.deltaXZ != 0)) {
            //We check if the player is in ground, since theoretically the y should be zero.
            float predicted = data.playerInfo.clientGround ? 0 : (data.playerInfo.lDeltaY - 0.08f) * .98f;

            if(data.playerInfo.lClientGround || data.playerInfo.collidesVertically) {
                predicted = Math.min(data.playerInfo.deltaY, MovementUtils.getJumpHeight(packet.getPlayer()));
            }

            if(Math.abs(predicted) < 0.005) predicted = 0;

            if(!data.playerInfo.flightCancel
                    && !data.playerInfo.clientGround
                    && !data.playerInfo.collidesVertically
                    && MathUtils.getDelta(data.playerInfo.deltaY, predicted) > 0.0001) {
                vl++;
                flag("deltaY=" + data.playerInfo.deltaY + " predicted=" + predicted);
            }

            debug("deltaY=" + data.playerInfo.deltaY + " predicted=" + predicted
                    + " ground=" + data.playerInfo.clientGround);
        }
    }
}
