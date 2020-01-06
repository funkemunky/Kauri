package dev.brighten.anticheat.check.impl.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.TickTimer;
import cc.funkemunky.api.utils.objects.Interval;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import lombok.val;

@CheckInfo(name = "Aim (D)", description = "Designed to detect Vape's aimassist.",
        checkType = CheckType.AIM, punishVL = 20, developer = true, executable = false)
public class AimD extends Check {

    private int verbose, zeroTicks;
    private TickTimer lastYChange = new TickTimer(10);

    @Packet
    public void onPacket(WrappedInFlyingPacket packet) {
        if (packet.isLook()) {
            long deltaX = data.moveProcessor.deltaX, deltaY = data.moveProcessor.deltaY;
            long dxx = Math.abs(deltaX - data.moveProcessor.lastDeltaX);

            if(deltaY != data.moveProcessor.lastDeltaY) lastYChange.reset();

            if(deltaY > 2 || deltaX < 25 || dxx > 14 || lastYChange.hasPassed()) {
                verbose = 0;
            } else if(verbose++ > 14) {
                vl++;
                flag("dx=%1 dy=%2 vb=%3", deltaX, deltaY, verbose);
            }

            debug("dx=%1 dy=%2 verbose=%3 yChange=%4", deltaX, deltaY, verbose, lastYChange.getPassed());
        }
    }
}
