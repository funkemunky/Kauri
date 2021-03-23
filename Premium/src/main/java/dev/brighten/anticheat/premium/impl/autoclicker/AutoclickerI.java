package dev.brighten.anticheat.premium.impl.autoclicker;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.KauriVersion;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Autoclicker (I)", description = "Checks for improper clicking from the client.",
        checkType = CheckType.AUTOCLICKER, punishVL = 30, planVersion = KauriVersion.ARA)
@Cancellable(cancelType = CancelType.INTERACT)
public class AutoclickerI extends Check {

    private long lastFlying, lastArm;
    private int buffer;

    @Packet
    public void use(WrappedInArmAnimationPacket packet, long now) {
        long delta = now - lastFlying;

        if(delta < 10 && now - lastArm > 30) {
            if(++buffer > 6) {
                vl++;
                flag("delta=%s buffer=%s", delta, buffer);
            }
        }

        debug("arm=%s", now);
        lastArm = now;
    }

    @Packet
    public void onUse(WrappedInUseEntityPacket packet, long now) {
        debug("use=%s", now);
    }

    @Packet
    public void flying(WrappedInFlyingPacket packet, long now) {
        if(data.playerInfo.lastTeleportTimer.isPassed(0) && now - lastFlying > 30)
        lastFlying = now;
    }
}
