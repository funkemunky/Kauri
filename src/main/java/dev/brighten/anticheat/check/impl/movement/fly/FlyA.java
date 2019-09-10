package dev.brighten.anticheat.check.impl.movement.fly;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;

@CheckInfo(name = "Fly (Type A)", description = "It's a fly check. Isn't that cool?")
public class FlyA extends Check {

    @Packet
    public void onMove(WrappedInFlyingPacket packet) {
        if(packet.isPos()) {
            float predicted = (data.playerInfo.lDeltaY - 0.08f) * 0.98f;

            if(Math.abs(predicted) < 0.005) predicted = 0;

            float delta = MathUtils.getDelta(predicted, data.playerInfo.deltaY);
            if(delta > 0.01 && data.playerInfo.airTicks > 2 && !data.playerInfo.flightCancel && !data.blockInfo.onClimbable) {
                if(vl++ > 40) {
                    punish();
                } else if(vl > 2) flag("ping=%p tps=%t delta=" + MathUtils.round(MathUtils.getDelta(data.playerInfo.deltaY, data.playerInfo.pDeltaY), 5));
            } else vl-= vl > 0 ? 0.25f : 0;
            debug("deltaY=" + data.playerInfo.deltaY + " pDeltaY=" + data.playerInfo.pDeltaY + " onGround=" + data.playerInfo.serverGround + " collidedVert=" + data.playerInfo.collidesVertically);
        }
    }
}
