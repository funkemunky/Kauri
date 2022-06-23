package dev.brighten.anticheat.check.impl.movement.fly;

import cc.funkemunky.api.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;

@CheckInfo(name = "Fly (I)", description = "Checks for bad hovering.",
        checkType = CheckType.FLIGHT, devStage = DevStage.BETA, punishVL = 8)
@Cancellable
public class FlyI extends Check {

    private int buffer;

    /*
        T1: add net.minecraft.world.entity.Entity.getPose() to check if player is really swimming (1.14 - latest)
         if not set to false cause then the player cant really swim
     */

    @Packet
    public void onFlying(WrapperPlayClientPlayerFlying packet) {
        if(!packet.hasPositionChanged()
                || data.playerInfo.generalCancel
                || (data.playerInfo.serverGround
                        || (data.playerInfo.clientGround && data.playerInfo.groundTicks < 3))
                || data.playerInfo.lastToggleFlight.isNotPassed(10)
                || data.playerInfo.lastVelocity.isNotPassed(1)
                || data.playerInfo.doingVelocity
                || data.playerInfo.deltaY != 0
                || data.playerInfo.canUseElytra
                || (data.blockInfo.inWater /* && Todo: T1 */)
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
