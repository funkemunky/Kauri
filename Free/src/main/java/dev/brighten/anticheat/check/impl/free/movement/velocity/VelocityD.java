package dev.brighten.anticheat.check.impl.free.movement.velocity;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.anticheat.utils.timer.Timer;
import dev.brighten.anticheat.utils.timer.impl.TickTimer;
import dev.brighten.api.KauriVersion;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;

@CheckInfo(name = "Velocity (D)", description = "Checks if a player responded to velocity",
        checkType = CheckType.VELOCITY, punishVL = 5, planVersion = KauriVersion.FREE, devStage = DevStage.BETA)
@Cancellable(cancelType = CancelType.MOVEMENT)
public class VelocityD extends Check {

    private int buffer;
    private double vY;
    private Timer lastVelocity = new TickTimer();

    @Setting(name = "bufferThreshold")
    private static int bufferThreshold = 3;

    @Packet
    public void onVelocity(WrappedOutVelocityPacket packet) {
        if(packet.getId() == data.getPlayer().getEntityId() && packet.getY() > 0.1) {
            data.runKeepaliveAction(ka -> {
                vY = packet.getY();
                lastVelocity.reset();
            });
        }
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(vY <= 0) return;

        //Player took velocity
        if(Math.abs(data.playerInfo.deltaY - vY) < 1E-4) {
            if(buffer > 0) buffer--;
            debug("Reset velocity: dy=%.4f vy=%.4f", data.playerInfo.deltaY, vY);
            vY = 0;
            return;
        }

        //All potential causes of false positives
        if(data.playerInfo.doingBlockUpdate
                || data.playerInfo.webTimer.isNotPassed(3)
                || data.playerInfo.liquidTimer.isNotPassed(3)
                || data.playerInfo.slimeTimer.isNotPassed(2)
                || data.blockInfo.inScaffolding
                || data.blockInfo.inHoney
                || data.playerInfo.blockAboveTimer.isNotPassed(2)) {
            vY = 0;
            debug("Potential false flag");
            return;
        }

        if(lastVelocity.isPassed(4)) {
            if(++buffer > 2) {
                vl++;
                flag("lv=%s", lastVelocity.getPassed());
            }
            vY = 0;
        }
    }
}
