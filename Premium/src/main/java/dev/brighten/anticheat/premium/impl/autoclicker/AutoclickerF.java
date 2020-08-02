package dev.brighten.anticheat.premium.impl.autoclicker;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Autoclicker (F)", description = "Checks for a constant skew of values.", developer = true,
        checkType = CheckType.AUTOCLICKER, maxVersion = ProtocolVersion.V1_8_9)
public class AutoclickerF extends Check {

    public int flying, buffer, ticks;
    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        flying++;
    }

    @Packet
    public void onUse(WrappedInUseEntityPacket packet) {
        ticks = 0;
    }

    @Packet
    public void onArm(WrappedInArmAnimationPacket packet) {
        if(data.playerInfo.breakingBlock)
            ticks = 0;

        if(flying > 3) ticks = 0;
        else ticks++;

        debug("flying=%v ticks=%v", flying, ticks);
        flying = 0;
    }
}
