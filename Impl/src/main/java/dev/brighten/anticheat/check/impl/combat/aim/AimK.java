package dev.brighten.anticheat.check.impl.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.EvictingList;
import dev.brighten.anticheat.utils.GraphUtil;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Aim (K)", description = "Graphical aim-check", checkType = CheckType.AIM)
public class AimK extends Check {

    private final EvictingList<Float> pitchSamples = new EvictingList<>(10),
            yawSamples = new EvictingList<>(10);

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if (packet.isLook() && !data.playerInfo.serverPos) {
            float yawDelta = MathUtils.getAngleDelta(data.playerInfo.to.yaw, data.playerInfo.from.yaw);
            float pitchDelta = data.playerInfo.deltaPitch;

            if (yawDelta > 0.0 && pitchDelta > 0.0) {
                this.pitchSamples.add(pitchDelta);
                this.yawSamples.add(yawDelta);

                GraphUtil.GraphResult pitchGraph = GraphUtil.getGraph(pitchSamples);
                GraphUtil.GraphResult yawGraph = GraphUtil.getGraph(yawSamples);

                double ratioPitch = pitchGraph.getPositives() * pitchGraph.getNegatives();
                double ratioYaw = yawGraph.getPositives() * yawGraph.getNegatives();

                if (ratioPitch == 0 && ratioYaw == 0) {
                    vl++;
                    if(vl > 20) {
                        this.flag(pitchGraph.getPositives() + ", " + pitchGraph.getNegatives()
                                + "," + yawGraph.getPositives() + ", " + yawGraph.getNegatives());
                    }
                } else vl-= vl > 0 ? 0.025f : 0;

                debug("pitch=%1 yaw=%2 vl=%3", ratioPitch, ratioYaw, vl);
            }
        }
    }
}