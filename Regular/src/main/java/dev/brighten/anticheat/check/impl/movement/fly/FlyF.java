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

        double max = (data.playerInfo.lastVelocity.isNotPassed(20)
                ? Math.max(data.playerInfo.velocityY, data.playerInfo.jumpHeight)
                : data.playerInfo.jumpHeight) + 0.001;

        if(data.playerInfo.lastHalfBlock.isNotPassed(20)
                || data.blockInfo.collidesHorizontally) max = Math.max(0.5625, max);

        if(data.playerInfo.deltaY > max
                && !data.blockInfo.roseBush
                && data.playerInfo.lastVelocity.isPassed(2)
                && !data.playerInfo.doingVelocity
                && data.playerInfo.slimeTimer.isPassed(10)
                && !data.playerInfo.generalCancel) {
            ++vl;
            flag("dY=%.3f max=%.3f", data.playerInfo.deltaY, max);
        }

        debug("halfBlock=%s ticks=%s", data.playerInfo.lastHalfBlock.getPassed(), data.blockInfo.onHalfBlock);
    }

}
