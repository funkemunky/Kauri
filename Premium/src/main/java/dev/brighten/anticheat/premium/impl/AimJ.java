package dev.brighten.anticheat.premium.impl;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.types.MathHelper;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.MiscUtils;
import dev.brighten.api.check.CheckType;
import lombok.val;

import java.util.ArrayList;
import java.util.List;

@CheckInfo(name = "Aim (J)", description = "Prediction.", checkType = CheckType.AIM, developer = true)
public class AimJ extends Check {

    private int buffer;
    private EvictingList<Float> deltas = new EvictingList<>(20);

    @Packet
    public void onLook(WrappedInFlyingPacket packet) {
        if(packet.isLook()) {
            float start = data.moveProcessor.sensitivityY * 0.6f + .2f;

            float tri = start * start * start * 8;
            float xUse = data.moveProcessor.deltaX * tri;
            float predicted = data.playerInfo.from.yaw + xUse * .15f;
            float yaw = data.playerInfo.to.yaw;
            float xDelta = Math.abs(yaw - predicted);

            if(data.moveProcessor.sensXPercent == data.moveProcessor.sensYPercent) {
                deltas.add(xDelta);

                if(xDelta > 0.001) {
                    vl++;
                    flag("delta=%v.2", xDelta);
                }

                debug("one=%v two=%v", xDelta, data.moveProcessor.deltaX);
            } else debug("sensX=%v sensY=%v", data.moveProcessor.sensXPercent, data.moveProcessor.sensYPercent);

            //debug("yaw=%v.1 pdelta=%v.2 deltaX=%v yawDelta=%v.3 sens=%v", yaw,
            //        xDelta, data.moveProcessor.deltaX, data.playerInfo.deltaYaw, data.moveProcessor.sensXPercent);
        }
    }
}