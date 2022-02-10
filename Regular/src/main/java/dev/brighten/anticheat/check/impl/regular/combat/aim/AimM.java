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
    private float ldeltaY;
    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(!packet.isLook()) return;

        float delta = Math.abs(ldeltaY - data.moveProcessor.deltaY);

        if(delta > 45 &&
                ((ldeltaY < 0 && data.moveProcessor.deltaY >= 0)
                        || (ldeltaY >= 0 && data.moveProcessor.deltaY < 0))) {
            if(++buffer > 10) {
                vl++;
                flag("%.3f;%.1f;%.1f;%.1f",delta, data.moveProcessor.deltaY, ldeltaY, data.moveProcessor.deltaX);
            }
        } else if(buffer > 0) buffer-= 0.75f;

        debug("d=%.4f p=%.4f b=%s", delta, data.moveProcessor.deltaY, buffer);

        ldeltaY = data.moveProcessor.deltaY;
    }
}
