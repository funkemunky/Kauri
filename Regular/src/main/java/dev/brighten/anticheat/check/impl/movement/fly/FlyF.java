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
    public void onPacket(WrappedInFlyingPacket packet, long current) {
        if(data.playerInfo.deltaXZ == 0 && data.playerInfo.deltaY == 0) return;

        double max = data.playerInfo.lastVelocity.isNotPassed(20)
                ? Math.max(data.playerInfo.velocityY, data.playerInfo.jumpHeight) : data.playerInfo.jumpHeight;

        if(data.playerInfo.deltaY > max && !data.playerInfo.serverGround
                && !data.playerInfo.gliding
                && data.playerInfo.lastTeleportTimer.isPassed(1)
                && current - data.creation > 5000L
                && !data.playerInfo.riptiding
                && !data.getPlayer().getAllowFlight()) {
            ++vl;
            flag("dY=%v.3 max=%v.3", data.playerInfo.deltaY, max);
        }
    }

}
