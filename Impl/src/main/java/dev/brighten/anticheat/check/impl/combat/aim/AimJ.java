package dev.brighten.anticheat.check.impl.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.processing.MovementProcessor;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Aim (J)", description = "Checks for strange mouse movements", checkType = CheckType.AIM, developer = true)
public class AimJ extends Check {
    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(packet.isLook() && data.playerInfo.deltaYaw > 0.0 && data.playerInfo.deltaPitch > 0.0 && data.playerInfo.deltaPitch < 1.0D && data.moveProcessor.deltaX > 0) {
            double deltaX = Math.abs(data.moveProcessor.deltaX - data.moveProcessor.lastDeltaX);
            double deltaY = Math.abs(data.moveProcessor.deltaY - data.moveProcessor.lastDeltaY);

            if (data.moveProcessor.deltaX >= 10 && data.moveProcessor.deltaX <= 20 && (data.moveProcessor.deltaY == 1 || data.moveProcessor.deltaY == 2) && deltaX >= 2.0 && deltaY == 0.0) {
                flag("dx=" + data.moveProcessor.deltaX + ", dy=" + data.moveProcessor.deltaY + ", mDx=" + deltaX + ", mDy=" + deltaY);
            }

            debug("yaw=" + data.moveProcessor.deltaX
                    + "pitch= " + data.moveProcessor.deltaY
                    + " sens=" + MovementProcessor.sensToPercent(data.moveProcessor.sensitivityX)
                    + " mYaw= " + deltaX
                    + " mPitch= " + deltaY
                    + " dYaw=" + data.playerInfo.deltaYaw
                    + " dPitch=" + data.playerInfo.deltaPitch
                    + " vl=" + vl + " pos=" + data.playerInfo.serverPos);
        }
    }
}
