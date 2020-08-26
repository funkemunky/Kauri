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

@CheckInfo(name = "Aim (I)", developer = true, punishVL = 20, checkType = CheckType.AIM, description = "")
public class AimI extends Check {

    private boolean looked;
    private float lYawMode, lPitchMode;
    private TickTimer lastModeChange = new TickTimer(20);
    @Packet
    public void onPacket(WrappedInFlyingPacket packet) {
        if (!packet.isLook()) return;

        float pdx = Math.abs(data.playerInfo.deltaYaw) / data.moveProcessor.yawMode;
        float pdy = Math.abs(data.playerInfo.deltaPitch) / data.moveProcessor.pitchMode;

        float rymode = MathUtils.round(data.moveProcessor.yawMode, 2),
                rpmode = MathUtils.round(data.moveProcessor.pitchMode, 2);

        if(rymode != lYawMode || rpmode != lPitchMode) lastModeChange.reset();

        float deltaX = Math.abs(MathHelper.floor_float(pdx) - pdx),
                deltaY = Math.abs(MathHelper.floor_float(pdy) - pdy);

        boolean flagX = deltaX < 0.92 && deltaX > 0.08, flagY = deltaY < 0.92 && deltaY > 0.08;

        if((flagX && flagY) && lastModeChange.hasPassed(20)) {
            vl++;
            debug(Color.Green + "Flag (%v): %v, %v", vl, flagX, flagY);
        } else {
            vl = 0;
            if(flagX && flagY) debug(Color.Red + "Did not flag due to sensitivity change");
        }

        debug("ymode=%v.2 pmode=%v.2 pdx=%v.1 pdy=%v.1 deltaX=%v.2 deltaY=%v.2",
                data.moveProcessor.yawMode, data.moveProcessor.pitchMode, pdx, pdy, deltaX, deltaY);

        lYawMode = rymode;
        lPitchMode = rpmode;
    }
}
