package dev.brighten.anticheat.check.impl.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.check.impl.combat.hitbox.ReachB;
import dev.brighten.anticheat.utils.timer.Timer;
import dev.brighten.anticheat.utils.timer.impl.TickTimer;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;

@CheckInfo(name = "Aim (N)", description = "Unnatural aim timing", checkType = CheckType.AIM,
        devStage = DevStage.ALPHA)
public class AimN extends Check {
    private Timer lastLook = new TickTimer();
    private int buffer;
    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(!packet.isLook()) return;
        long delta = lastLook.getPassed();
        long lAim = find(ReachB.class).lastAimOnTarget.getPassed();

        if(delta > 1 && delta < 20 && lAim < 3) {
            if(++buffer > 5) {
                vl++;
                flag(120, "%s;%s", delta, lAim);
            }
        } else if(buffer > 0) buffer-= 3;

        debug("t=%s a=%s b=%s", delta, lAim, buffer);
        lastLook.reset();
    }
}
