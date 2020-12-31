package dev.brighten.anticheat.check.impl.movement.velocity;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.Color;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.KauriVersion;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Velocity (D)", description = "Checks if a player takes no velocity",
        checkType = CheckType.VELOCITY, developer = true, planVersion = KauriVersion.FREE)
@Cancellable(cancelType = CancelType.MOVEMENT)
public class VelocityD extends Check {

    private int buffer;
    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(data.playerInfo.lastVelocity.isPassed(1)) return;

        if(data.playerInfo.deltaY < 0.001
                && data.playerInfo.deltaY >= 0
                && data.playerInfo.velocityY > 0.2) {
            buffer+= 3;

            if(buffer > 10) {
                vl++;
                flag("dy=%v.4 vy=%v.4", data.playerInfo.deltaY, data.playerInfo.velocityY);
                buffer = 9;
            }
            debug(Color.Green + "Flag");
        } else if(buffer > 0) buffer--;

        debug("dy=%v.4 vY=%v.4", data.playerInfo.deltaY, data.playerInfo.velocityY);
    }
}
