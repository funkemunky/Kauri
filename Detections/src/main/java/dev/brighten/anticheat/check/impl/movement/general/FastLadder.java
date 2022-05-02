package dev.brighten.anticheat.check.impl.movement.general;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.utils.timer.Timer;
import dev.brighten.anticheat.utils.timer.impl.PlayerTimer;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "FastLadder", description = "Ensures players do not go faster than legitimate speeds on ladders.",
        checkType = CheckType.GENERAL, executable = true, punishVL = 10)
@Cancellable
public class FastLadder extends Check {

    private Timer lastJump;

    @Override
    public void setData(ObjectData data) {
        super.setData(data);
        lastJump = new PlayerTimer(data, 6);
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(packet.isPos()
                && data.playerInfo.lastVelocity.isPassed(10)
                && data.blockInfo.onClimbable
                && !data.playerInfo.generalCancel) {
            if(data.playerInfo.jumped) lastJump.reset();
            if(data.playerInfo.deltaY > (lastJump.isNotPassed() ? data.playerInfo.jumpHeight : 0.144)) {
                if((vl+=(data.playerInfo.deltaY > data.playerInfo.jumpHeight * 1.5 ? 10 : 1)) > 8) {
                    flag("deltaY=" + data.playerInfo.deltaY);
                }
            } else vl-= vl > 0 ? 0.5f : 0;

            debug("deltaY=" + data.playerInfo.deltaY + " vl=" + vl);
        } else vl-= vl > 0 ? 0.02f : 0;
    }
}
