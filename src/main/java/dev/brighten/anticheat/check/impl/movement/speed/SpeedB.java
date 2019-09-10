package dev.brighten.anticheat.check.impl.movement.speed;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;

@CheckInfo(name = "Speed (Type B)", description = "Ensures acceleration is legit.")
public class SpeedB extends Check {

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(!packet.isPos() || data.playerInfo.generalCancel) return;
        float accel = data.playerInfo.deltaXZ - data.playerInfo.lDeltaXZ;
        float predicted = data.playerInfo.pDeltaXZ - data.playerInfo.lpDeltaXZ;
        float delta = MathUtils.getDelta(accel, predicted) * 250;

        if(data.playerInfo.airTicks > 1 || data.playerInfo.groundTicks > 0) {

            if(delta > 1) {
                //debug(Color.Green + "Flag");
                vl++;

                if(vl > 50) {
                   // punish();
                } else if(vl > 10) {
                    //flag("delta=" + delta);
                }
            } else vl-= vl > 0 ? 2 : 0;
            debug("accel=" + accel + " predicted=" + predicted + " delta=" + delta);
        }
    }
}
