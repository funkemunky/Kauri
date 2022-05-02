package dev.brighten.anticheat.check.impl.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;

@CheckInfo(name = "Aim (C)", description = "Checks for bad client rotations.", checkType = CheckType.AIM,
        devStage = DevStage.BETA, enabled = false)
public class AimC extends Check {
    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(packet.isLook() && data.moveProcessor.deltaX > 0) {
            float deltaYaw = MathUtils.getAngleDelta(data.playerInfo.to.yaw, data.playerInfo.from.yaw);
            if(deltaYaw > 320
                    && data.playerInfo.lDeltaYaw > 0
                    && data.moveProcessor.sensitivityX < 0.65
                    && data.playerInfo.lDeltaYaw < 30
                    && data.playerInfo.lastTeleportTimer.isPassed(1)
                    && !data.playerInfo.doingTeleport) {
                vl++;
                if(vl > 1) {
                    flag("yaw=%.3f", deltaYaw);
                }
            } else vl-= vl > 0 ? 0.005 : 0;
            debug("deltaX=%s yaw=%s lyaw=%s sens=%s vl=%s",
                    data.moveProcessor.deltaX, data.playerInfo.to.yaw, data.playerInfo.from.yaw,
                    data.moveProcessor.sensXPercent, vl);
        }
    }
}
