package dev.brighten.anticheat.premium.impl.autoclicker;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
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

    private int arm;

    @Packet
    public void use(WrappedInArmAnimationPacket packet, long now) {
        debug("arm=%s", now);
    }

    @Packet
    public void flying(WrappedInFlyingPacket packet, long now) {
        debug("flying=%s", now);
    }
}
