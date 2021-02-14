package dev.brighten.anticheat.check.impl.movement.fly;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.KauriVersion;
import dev.brighten.api.check.CheckType;

@Cancellable
@CheckInfo(name = "Fly (F)", description = "Checks if an individual flys faster than possible.",
        checkType = CheckType.FLIGHT, planVersion = KauriVersion.FULL, developer = true)
public class FlyF extends Check {

    @Packet
    public void onPacket(WrappedInFlyingPacket packet) {
        if(data.playerInfo.deltaXZ == 0 && data.playerInfo.deltaY == 0) return;

        double max = Math.max(data.playerInfo.calcVelocityY, data.playerInfo.jumpHeight);

        if(data.playerInfo.deltaY > max
                && !data.playerInfo.flightCancel) {
            ++vl;
            flag("dY=%.3f max=%.3f", data.playerInfo.deltaY, max);
        }
    }

}
