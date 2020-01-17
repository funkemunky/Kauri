package dev.brighten.anticheat.check.impl.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.EvictingList;
import dev.brighten.anticheat.utils.GraphUtil;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Aim (K)", description = "Graphical aim-check", checkType = CheckType.AIM)
public class AimK extends Check {

    private final EvictingList<Float> pitchSamples = new EvictingList<>(20), yawSamples = new EvictingList<>(20);

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if (packet.isLook() && !data.playerInfo.serverPos) {
            float yawDelta = data.playerInfo.deltaYaw;
            float pitchDelta = data.playerInfo.deltaPitch;

            if (yawDelta > 0.0 && pitchDelta > 0.0) {
                this.pitchSamples.add(pitchDelta);
                this.yawSamples.add(yawDelta);

                GraphUtil.GraphResult pitchGraph = GraphUtil.getGraph(pitchSamples);
                GraphUtil.GraphResult yawGraph = GraphUtil.getGraph(yawSamples);

                if (pitchGraph.getPositives() == 0.0 || pitchGraph.getNegatives() == 0.0) {
                    return;
                }

                double ratioPitch = (double) (pitchGraph.getNegatives() / pitchGraph.getPositives());
                double ratioYaw = (double) (yawGraph.getNegatives() / yawGraph.getPositives());

                if (ratioPitch == 0 || ratioYaw == 0) {
                    vl++;
                    if(vl > 7) {
                        this.flag(ratioPitch + ", " + ratioYaw);
                    }
                } else vl-= vl > 0 ? 0.2f : 0;

                debug("pitch=%1 yaw=%2", ratioPitch, ratioYaw);
            }
        }
    }
}