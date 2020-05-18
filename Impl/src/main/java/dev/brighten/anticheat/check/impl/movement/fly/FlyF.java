package dev.brighten.anticheat.check.impl.movement.fly;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.Helper;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Fly (F)", description = "Ensures a user doesn't fly faster than the maximum threshold.",
        checkType = CheckType.FLIGHT, punishVL = 5, enabled = false)
@Cancellable
public class FlyF extends Check {

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(packet.isPos()) {
            double threshold = Math.max(0.8, data.playerInfo.jumpHeight * 2);

            if(data.playerInfo.deltaY > threshold
                    && !data.playerInfo.canFly
                    && !data.playerInfo.flightCancel
                    && !data.playerInfo.creative
                    && !data.playerInfo.wasOnSlime) {
                vl++;
                flag("deltaY=%v;threshold=%v",
                        Helper.format(data.playerInfo.deltaY, 2), Helper.format(threshold, 2));
            }
        }
    }
}
