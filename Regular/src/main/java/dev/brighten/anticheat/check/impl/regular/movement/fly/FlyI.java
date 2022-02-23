package dev.brighten.anticheat.check.impl.regular.movement.fly;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;

@CheckInfo(name = "Fly (I)", description = "Checks for bad hovering.",
        checkType = CheckType.FLIGHT, devStage = DevStage.ALPHA, punishVL = 8)
@Cancellable
public class FlyI extends Check {

    /*
    if (!packet.isPos()
				|| !canCheckMovement()
				|| (isAccurateOnGround(packet) && data.enviorment.inLiquid.wasNotReset())
				|| (isAccurateOnGround(packet) && data.enviorment.inLiquid.wasReset())
				|| data.timers.lastFlightToggle.hasNotPassed(10)
				|| data.timers.lastBlockGlitch.hasNotPassed(5)
				|| data.enviorment.onLadder.hasNotPassed(2)
				|| data.velocity.deltaV != 0
				|| data.movement.deltaV != 0) {
			debug("RESET");
			hoverFails = 0;
			return;
		}

		if (hoverFails++ == 6) {
			hoverFails = 0;
			if (data.timers.join.hasPassed(20)) {
				if (fail()) setback(1);
			}
		}
		debug("%s | V: %s", hoverFails, data.movement.deltaV);
     */

    private int buffer;
    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(!packet.isPos()
                || data.playerInfo.generalCancel
                || (data.playerInfo.serverGround
                        || (data.playerInfo.clientGround && data.playerInfo.groundTicks < 3))
                || data.playerInfo.lastToggleFlight.isNotPassed(10)
                || data.playerInfo.lastVelocity.isNotPassed(1)
                || data.playerInfo.doingVelocity
                || data.playerInfo.deltaY != 0
                || data.playerInfo.climbTimer.isNotPassed(2)) {
            buffer = 0;
            debug("Resetting buffer");
        }

        if(++buffer > 6) {
            vl++;
            flag("b=%s a=%s", buffer, data.playerInfo.airTicks);
        }
    }
}
