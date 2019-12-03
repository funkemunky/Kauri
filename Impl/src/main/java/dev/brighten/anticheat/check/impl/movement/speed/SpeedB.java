package dev.brighten.anticheat.check.impl.movement.speed;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;

@CheckInfo(name = "Speed (B)", description = "Predicts the motion of a player accurately.", developer = true,
        executable = false, punishVL = 150)
public class SpeedB extends Check {

    private int moveTicks;
    @Packet
    public void onPacket(WrappedInFlyingPacket packet) {
        if(packet.isPos() && data.playerInfo.deltaXZ > 0) {
            if(moveTicks < 40) {
                moveTicks++; //jank way to prevent false positives until a player moves a bit but works.
            } else {
                double diff = data.predictionService.diff;

                if(diff > 0.00001
                        && data.playerInfo.deltaXZ > MathUtils
                        .hypot(data.predictionService.predX, data.predictionService.predZ)
                        && !data.playerInfo.flightCancel
                        && data.playerInfo.blocksAboveTicks == 0
                        && !data.playerInfo.collidesHorizontally
                        && data.playerInfo.lastToggleFlight.hasPassed(20)) {
                    vl++;
                    if(diff > 0.6f || vl > 3) {
                        flag("diff=" + MathUtils.round(diff, 4)
                                + " xz=" + MathUtils.round(data.playerInfo.deltaXZ, 4));
                    }
                    debug(Color.Green + "Flag");
                } else vl-= vl > 0 ? 0.2f : 0;

                debug("diff=" + diff + " vl=" + vl + " sneak=" + data.playerInfo.sneaking + " sprinting=" + data.playerInfo.sprinting);
            }
        }
    }
}
