package dev.brighten.anticheat.check.impl.combat.hand;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockPlacePacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Hand (A)", description = "Checks for irregular block place packets.",
        checkType = CheckType.HAND, punishVL = 30, executable = false)
@Cancellable(cancelType = CancelType.INTERACT)
public class HandA extends Check {

    private long lastFlying;
    private boolean arm, placed;

    @Packet
    public void onBlockPlace(WrappedInBlockPlacePacket place, long timeStamp) {
        debug("place sent arm=%v ticks=%v", arm, lastFlying);
        placed = true;
        lastFlying = 0;
    }

    @Packet
    public void onArm(WrappedInArmAnimationPacket packet) {
        debug("arm packet sent placed=%v", placed);
        arm = true;
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        lastFlying++;
        if(arm) {
            debug("arm is true");
        }
        if(placed) {
            debug("placed is true");
        }
        arm = placed = false;
    }
}
