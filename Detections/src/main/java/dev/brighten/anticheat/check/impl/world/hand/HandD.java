package dev.brighten.anticheat.check.impl.world.hand;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Hand (D)", description = "Checks for a player not swinging their arm while attacking",
        checkType = CheckType.HAND, punishVL = 7, executable = true)
@Cancellable(cancelType = CancelType.ATTACK)
public class HandD extends Check {

    private int attackTicks;

    @Packet
    public void onFlying(WrappedInUseEntityPacket packet) {
        if(!packet.getAction().equals(WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK)) return;

        if(attackTicks++ > 3
                && data.playerInfo.lastFlyingTimer.isNotPassed(1)
                && !data.lagInfo.lagging) {
            vl++;
            flag("has not swung since %s ticks", attackTicks);
        }
    }

    @Packet
    public void onFlying(WrappedInArmAnimationPacket packet) {
        attackTicks = 0;
    }
}
