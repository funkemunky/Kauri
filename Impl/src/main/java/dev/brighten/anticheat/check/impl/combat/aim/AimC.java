package dev.brighten.anticheat.check.impl.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.processing.MovementProcessor;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Aim (C)", description = "Checks for bad client rotations.", checkType = CheckType.AIM)
public class AimC extends Check {

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(packet.isLook() && data.moveProcessor.deltaX > 0) {
            if(data.playerInfo.deltaYaw > 280
                    && data.moveProcessor.deltaX > 1000
                    && data.playerInfo.lDeltaYaw < 20
                    && !data.playerInfo.serverPos) {
                vl++;
                if(vl > 1) {
                    flag("yaw=%1", data.playerInfo.deltaYaw);
                }
            } else vl-= vl > 0 ? 0.005 : 0;
            debug("yaw=" + data.moveProcessor.deltaX
                    + " sens=" + MovementProcessor.sensToPercent(data.moveProcessor.sensitivityX)
                    + " vl=" + vl + " pos=" + data.playerInfo.serverPos);
        }
    }
}
