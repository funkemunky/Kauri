package dev.brighten.anticheat.premium.impl;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Aim (G)", description = "Prediction.", checkType = CheckType.AIM, vlToFlag = 15, developer = true)
public class AimG extends Check {

    @Packet
    public void onLook(WrappedInFlyingPacket packet) {
        if(packet.isLook()) {
            if(data.moveProcessor.pitchGcdList.size() < 45) return;

            if(data.moveProcessor.sensXPercent == data.moveProcessor.sensYPercent) {
                float start = data.moveProcessor.sensitivityY * 0.6f + .2f;

                float tri = start * start * start * 8;
                float xUse = data.moveProcessor.deltaX * tri;
                float predicted = data.playerInfo.from.yaw + xUse * .15f;
                float yaw = data.playerInfo.to.yaw;

                debug("yaw=%v.1 predicted=%v.1 deltaX=%v", yaw, predicted, data.moveProcessor.deltaX);
            }
        }
    }
}