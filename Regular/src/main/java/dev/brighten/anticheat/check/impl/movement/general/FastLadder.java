package dev.brighten.anticheat.check.impl.movement.general;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.utils.TickTimer;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "FastLadder", description = "Ensures players do not go faster than legitimate speeds on ladders.",
        checkType = CheckType.GENERAL, executable = false, punishVL = 20)
@Cancellable
public class FastLadder extends Check {

    private TickTimer lastJump;

    @Override
    public void setData(ObjectData data) {
        super.setData(data);
        lastJump = new TickTimer( 6);
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(packet.isPos()
                && data.playerInfo.lastVelocity.hasPassed(10)
                && data.blockInfo.onClimbable
                && !data.playerInfo.generalCancel) {
            if(data.playerInfo.jumped) lastJump.reset();
            if(data.playerInfo.deltaY > (lastJump.hasNotPassed() ? data.playerInfo.jumpHeight : 0.144)) {
                if((vl+=(data.playerInfo.deltaY > data.playerInfo.jumpHeight * 1.5 ? 10 : 1)) > 8) {
                    flag("deltaY=" + data.playerInfo.deltaY);
                }
            } else vl-= vl > 0 ? 0.5f : 0;

            debug("deltaY=" + data.playerInfo.deltaY + " vl=" + vl);
        } else vl-= vl > 0 ? 0.02f : 0;
    }
}
