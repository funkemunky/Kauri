package dev.brighten.anticheat.check.impl.combat.autoclicker;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Autoclicker (A)", description = "A fast click check.", checkType = CheckType.AUTOCLICKER,
        punishVL = 50)
@Cancellable(cancelType = CancelType.INTERACT)
public class AutoclickerA extends Check {

    private int flyingTicks, cps;

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        flyingTicks++;
        if(flyingTicks >= 20) {
            if(cps >= 20) {
                if(cps > 26) vl++;
                flag("cps=%1", cps);
            }
            debug("cps=%1", cps);

            flyingTicks = cps = 0;
        }
    }

    @Packet
    public void onArmAnimation(WrappedInArmAnimationPacket packet) {
        if(!data.playerInfo.breakingBlock && data.playerInfo.lastBlockPlace.hasPassed(2))
            cps++;
    }
}
