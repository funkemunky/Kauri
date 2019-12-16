package dev.brighten.anticheat.check.impl.combat.autoclicker;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Autoclicker (E)", description = "Oscillation", checkType = CheckType.AUTOCLICKER)
public class AutoclickerE extends Check {

    private long lTimestamp;
    @Packet
    public void onClick(WrappedInArmAnimationPacket packet, long timeStamp) {

    }
}
