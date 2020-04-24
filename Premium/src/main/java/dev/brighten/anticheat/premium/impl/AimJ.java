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
            double xUse = data.moveProcessor.deltaX * tri;
            boolean greaterX = data.playerInfo.to.yaw - data.playerInfo.from.yaw > 0;
            double yaw = data.playerInfo.from.yaw + (xUse * .15 * (greaterX ? 1 : -1));
            double xDelta = MathUtils.getDelta(data.playerInfo.to.yaw, yaw);

            if(data.moveProcessor.sensXPercent == data.moveProcessor.sensYPercent
                    && data.moveProcessor.deltaX > 20
                    && data.moveProcessor.yawGcdList.size() > 35
                    && MathUtils.getDelta(xDelta, 0.05) > 0.01
                    && xDelta > 0.02) {
                vl++;
                flag("xdelta=%v.2 delta=%v", xDelta, data.moveProcessor.deltaX);
            }
            debug("xDelta=%v.2", xDelta);
        }
    }
}