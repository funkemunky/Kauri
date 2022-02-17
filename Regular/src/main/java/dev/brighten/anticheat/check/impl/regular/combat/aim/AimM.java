package dev.brighten.anticheat.check.impl.regular.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.KauriVersion;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;

@CheckInfo(name = "Aim (M)", description = "Aim snapping", checkType = CheckType.AIM, executable = true,
        planVersion = KauriVersion.FULL, punishVL = 25)
public class AimM extends Check {

    private float buffer;
    private double ldeltaY;
    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(!packet.isLook()) return;

        double deltaXY = Math.hypot(data.moveProcessor.deltaX, data.moveProcessor.deltaY);
        double accel = Math.abs(ldeltaY - deltaXY);

        debug("d=%.4f p=%.4f b=%s", accel, deltaXY, buffer);

        ldeltaY = deltaXY;
    }
}
