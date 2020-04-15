package dev.brighten.anticheat.premium.impl;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Aim (J)", description = "Prediction.", checkType = CheckType.AIM, developer = true)
public class AimJ extends Check {

    private int buffer;
    private EvictingList<Double> listX = new EvictingList<>(100);

    @Packet
    public void onLook(WrappedInFlyingPacket packet) {
        if(packet.isLook()) {
            double start = data.moveProcessor.sensitivityX * 0.6f + .2f;

            double tri = Math.pow(start, 3) * 8;
            double xUse = data.moveProcessor.deltaX * tri, yUse = data.moveProcessor.deltaY * tri;
            boolean greaterX = data.playerInfo.to.yaw - data.playerInfo.from.yaw > 0,
                    greaterY = data.playerInfo.deltaPitch > 0;
            double yaw = data.playerInfo.from.yaw + (xUse * .15 * (greaterX ? 1 : -1)),
                    pitch = data.playerInfo.from.pitch + (yUse * .15 * (greaterY ? 1 : -1));
            double xDelta = MathUtils.getDelta(data.playerInfo.to.yaw, yaw),
                    yDelta = MathUtils.getDelta(data.playerInfo.to.pitch, pitch);

            if(data.moveProcessor.sensXPercent == data.moveProcessor.sensYPercent) {
                listX.add(xDelta);
                listX.add(yDelta);
            }

            if(listX.size() >= 100) {
                long count = listX.parallelStream().filter(xd -> xd > 0.01).count();

                if(count > 50) {
                    vl++;
                    flag("count=%v", count);
                }
                debug("count=%v", count);
                listX.clear();
            }

            debug("xDelta=%v.2 yDelta=%v.2", xDelta, yDelta);
        }
    }
}