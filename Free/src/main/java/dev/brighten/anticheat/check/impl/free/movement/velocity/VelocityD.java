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

@CheckInfo(name = "Velocity (D)", description = "Checks if a player takes no velocity",
        checkType = CheckType.VELOCITY, punishVL = 3, planVersion = KauriVersion.FREE, executable = true)
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
            vY = packet.getY();
            lastVelocity.reset();
        }
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(vY <= 0) {
            buffer = 0;
            return;
        }

        //if the player took velocity, we reset.
        if(data.playerInfo.deltaY > 0 || !data.playerInfo.serverGround || !data.playerInfo.checkMovement) {
            vY = 0;
            buffer = 0;
            return;
        }

        if(data.playerInfo.blockAboveTimer.isPassed(4)
                && lastVelocity.isPassed(data.lagInfo.transPing + 3)
                && data.playerInfo.liquidTimer.isPassed(4)
                && data.playerInfo.webTimer.isPassed(4)
                && data.playerInfo.lastMoveTimer.isNotPassed(2)
                && data.lagInfo.lastPacketDrop.isPassed(5)) {

            if(++buffer > bufferThreshold) {
                vl++;
                flag("v=%.3f dy=%.3f g=%s", vY, data.playerInfo.deltaY, data.playerInfo.clientGround);
                vY = 0;
                buffer = 0;
            }

            debug("Flagged: " + buffer);
        } else buffer = 0;
    }
}
