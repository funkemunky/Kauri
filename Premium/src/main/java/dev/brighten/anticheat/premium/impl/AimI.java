package dev.brighten.anticheat.premium.impl;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MathHelper;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.TickTimer;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import lombok.val;

@CheckInfo(name = "Aim (I)", developer = true, punishVL = 20, checkType = CheckType.AIM,
        description = "Improper modulo.")
public class AimI extends Check {

    private int buffer;
    @Packet
    public void onPacket(WrappedInFlyingPacket packet) {
        if (!packet.isLook()) return;

        if(data.moveProcessor.sensXPercent != data.moveProcessor.sensYPercent) return;
        val pitch = unFixed(data.playerInfo.to.pitch);

        if(Math.abs(pitch) < 0.005) {
            if(++buffer > 14) {
                vl++;
                flag("pitch=%v", pitch);
            }
            debug(Color.Green + "Flagged");
        } else buffer = 0;

        debug("pitch=%v l=%v", pitch, String.valueOf(pitch).length());
    }

    private float unFixed(float whatever) {
        val f = data.moveProcessor.sensitivityX * 0.6f + .2f;
        val shit = f * f * f * 1.2f;

        return whatever % shit;
    }
}
