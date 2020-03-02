package dev.brighten.anticheat.check.impl.movement.fly;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Fly (F)", description = "Ensures a user doesn't fly faster than the maximum threshold.",
        checkType = CheckType.FLIGHT, punishVL = 3)
@Cancellable
public class FlyF extends Check {

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        if(packet.isPos()) {
            double jumpHeight = data.playerInfo.jumpHeight;

            if(data.playerInfo.deltaY > jumpHeight * 1.5
                    && !data.playerInfo.canFly
                    && !data.playerInfo.generalCancel
                    && !data.playerInfo.creative
                    && !data.playerInfo.wasOnSlime
                    && timeStamp - data.playerInfo.lastServerPos > 100
                    && !data.playerInfo.inVehicle
                    && timeStamp - data.creation > 4000
                    && !data.playerInfo.riptiding
                    && !data.playerInfo.gliding) {
                vl++;
                flag("deltaY=" + data.playerInfo.deltaY);
            }
        }
    }
}
