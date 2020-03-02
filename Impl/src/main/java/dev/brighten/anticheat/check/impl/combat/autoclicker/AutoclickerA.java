package dev.brighten.anticheat.check.impl.combat.autoclicker;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Autoclicker (A)", description = "A fast click check.", checkType = CheckType.AUTOCLICKER,
        punishVL = 50)
@Cancellable(cancelType = CancelType.INTERACT)
public class AutoclickerA extends Check {

    private int flyingTicks, cps;
    private long lastFlying;

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        if(timeStamp - lastFlying > 1) flyingTicks++;
        else cps-= cps > 0 ? 1 : 0;
        if(flyingTicks >= 20) {
            if(cps > 22) {
                if(cps > 28) vl++;
                flag("cps=%1", cps);
            }
            debug("cps=%1", cps);

            flyingTicks = cps = 0;
        }
        lastFlying = timeStamp;
    }

    @Packet
    public void onArmAnimation(WrappedInArmAnimationPacket packet) {
        if(!data.playerInfo.breakingBlock
                && !data.playerInfo.lookingAtBlock
                && data.playerInfo.lastBrokenBlock.hasPassed(5)
                && data.playerInfo.lastBlockPlace.hasPassed(2))
            cps++;
        debug("breaking=%1 lastBroken=%2", data.playerInfo.breakingBlock,
                data.playerInfo.lastBrokenBlock.getPassed());
    }
}
