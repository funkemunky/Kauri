package dev.brighten.anticheat.check.impl.movement.nofall;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;

@CheckInfo(name = "NoFall (A)", description = "Looks for impossible location updates compared to ground.",
        checkType = CheckType.NOFALL, devStage = DevStage.ALPHA, punishVL = 12, vlToFlag = 2, executable = true)
@Cancellable
public class NoFallA extends Check {

    private float buffer;

    /*
         T1: add net.minecraft.world.entity.Entity.getPose() to check if player is really swimming (1.14 - latest)
         if not set to false cause then the player cant really swim
     */

    private static double divisor = 1. / 64.;

    @Packet
    public void onPacket(WrappedInFlyingPacket packet) {
        if (data.playerInfo.generalCancel
                || !packet.isPos()
                || data.blockInfo.onBoat
                || (data.playerInfo.deltaXZ == 0 && data.playerInfo.deltaY == 0)
                || (data.blockInfo.inWater /* && Todo: T1 */)) {
            if (buffer > 0) buffer -= 0.5f;
            return;
        }

        boolean onGround = packet.isGround();
        boolean flag = false;

        if (onGround) {
            flag = Math.abs(data.playerInfo.deltaY) > 0.0051
                    // Precautionary since it causes falses with onGround = false
                    && data.playerInfo.slimeTimer.isPassed(2)
                    && data.playerInfo.blockAboveTimer.isPassed(3)
                    && !data.playerInfo.serverGround
                    && (data.playerInfo.deltaY >= 0
                    && (Math.abs(data.playerInfo.to.y) % divisor != 0
                        || Math.abs(data.playerInfo.deltaY) % divisor != 0)
                        // If player has touchdown, would be nasties
                        || (data.playerInfo.deltaY <= data.playerInfo.lDeltaY));
        } else {
            flag = data.playerInfo.deltaY == 0
                    && data.playerInfo.lDeltaY == 0
                    // Player can move around in air but not be on the ground if sneaking
                    && data.playerInfo.climbTimer.isPassed(3)
                    // For some reason causes false positives on slime blocks
                    && data.playerInfo.slimeTimer.isPassed(2);
        }

        if (flag) {
            if (++buffer > 1) {
                vl++;
                flag("g=%s;dy=%.4f;ldy=%.4f", onGround, data.playerInfo.deltaY, data.playerInfo.lDeltaY);
            }
        } else if (buffer > 0) buffer -= 0.25f;

        debug("[%.1f] g=%s;dy=%.4f;ldy=%.4f",
                buffer, onGround, data.playerInfo.deltaY, data.playerInfo.lDeltaY);
    }
}