package dev.brighten.anticheat.check.impl.movement.speed;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.TickTimer;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@Cancellable
@CheckInfo(name = "Speed (C)", description = "Horizontal prediction.",
        checkType = CheckType.SPEED, punishVL = 150, vlToFlag = 10)
public class SpeedC extends Check {

    private TickTimer lastFlag = new TickTimer(10);
    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        if(!packet.isPos() || data.playerInfo.deltaXZ == 0) return;

        if(!data.playerInfo.generalCancel && data.predictionService.checkConditions(data.playerInfo.sprinting)) {
            double deltaXZ = MathUtils.hypot(
                    data.playerInfo.to.x - data.playerInfo.from.x,
                    data.playerInfo.to.z - data.playerInfo.from.z);

            double percent = deltaXZ / MathUtils.hypot(data.predictionService.predX, data.predictionService.predZ)
                    * 100;

            if(!data.playerInfo.clientGround && !data.playerInfo.lClientGround && data.playerInfo.sprinting) return;

            if(MathUtils.getDelta(percent, 100) > 0.01 && percent > 30 && timeStamp - data.creation > 10000) {
                vl++;
                if(vl > 7) {
                    flag("pct=%1%", MathUtils.round(percent, 4));
                }
                lastFlag.reset();
            } else if(lastFlag.hasPassed()) vl-= vl > 0 ? 1 : 0;
            else vl-= vl > 0 ? 0.5f : 0;

            debug("pct=%1 deltaXZ=%2", percent, deltaXZ);
        }
    }
}