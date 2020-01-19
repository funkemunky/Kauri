package dev.brighten.anticheat.check.impl.movement.fly;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Fly (C)", description = "Ensures a player does not fly in a vehicle.",
        checkType = CheckType.FLIGHT, developer = true, punishVL = 10)
@Cancellable
public class FlyC extends Check {

    @Packet
    public void onPacket(WrappedInFlyingPacket packet) {
        if(data.playerInfo.inVehicle
                && !data.playerInfo.gliding
                && !data.playerInfo.riptiding
                && !data.playerInfo.serverGround) {
            double vaccel = data.playerInfo.deltaY - data.playerInfo.lDeltaY;

            if(vaccel > -0.02) {
                if(vl++ > 2) {
                    flag("v=%1 deltaY=%2", vaccel, data.playerInfo.deltaY);
                }
            } else vl-= vl > 0 ? 0.25f : 0;
        }
    }
}
