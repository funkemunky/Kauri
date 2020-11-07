package dev.brighten.anticheat.premium.impl;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.processing.MovementProcessor;
import dev.brighten.anticheat.utils.MiscUtils;
import dev.brighten.anticheat.utils.Verbose;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Aim (G)", description = "Checks for bad GCD bypasses. (Rhys collab)",
        checkType = CheckType.AIM, punishVL = 30)
public class AimG extends Check {

    @Packet
    public void process(WrappedInFlyingPacket packet) {
        if(!packet.isLook()) return;

        float fPitch = data.playerInfo.from.pitch;

        float sens = data.moveProcessor.sensitivityY;

        if(data.moveProcessor.sensYPercent != data.moveProcessor.sensXPercent) {
            debug("naughty pcts %v %v",
                    data.moveProcessor.sensXPercent, data.moveProcessor.sensYPercent);
            return;
        }

       /* float f = sens * 0.6f + .2f;
        float f2 = f * f * f * 8f;

        double deltaY = Math.abs(data.moveProcessor.deltaY) % 1;
        fPitch-= f2 * deltaY * .15f;

        boolean y = deltaY > 0.9 || deltaY < 0.1;

        if(!y) debug(Color.Green + "Flaggy");*/

        float tPitch = modulo(data.moveProcessor.sensitivityY, data.playerInfo.to.pitch);
        float delta = Math.abs(data.playerInfo.to.pitch - tPitch);

        if(delta < 0.008 && Math.abs(data.moveProcessor.deltaY) < 100) {
            vl++;
            flag("test");
            debug(Color.Green + " delta=%v deltaY=%v.1", delta, data.moveProcessor.deltaY);
        }

        debug("delta=%v sens=%v pcts=%v", delta, sens, data.moveProcessor.sensYPercent);
    }

    private static float modulo(float s, float angle) {
        float f = (s * 0.6f + .2f);
        float f2 = f * f * f * 1.2f;
        return angle - (angle % f2);
    }
}