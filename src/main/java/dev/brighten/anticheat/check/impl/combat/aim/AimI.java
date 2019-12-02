package dev.brighten.anticheat.check.impl.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.CheckType;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.processing.MovementProcessor;
import cc.funkemunky.api.utils.objects.Interval ;

@CheckInfo(name = "Aim (I)", description = "Checks pitch accel", checkType = CheckType.AIM)
public class AimI extends Check {

    private Interval interval = new Interval(30);
    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(packet.isLook()) {

            /*if(data.playerInfo.deltaYaw > 0 && data.playerInfo.lDeltaYaw > 0) {
                if(interval.size() > 20) {
                    debug("min=" + interval.min() + " avg=" + interval.average() + " std=" + interval.std());
                    interval.clear();
                } else interval.add(data.moveProcessor.deltaX);*/

            if(data.playerInfo.deltaX > 100 && data.playerInfo.deltaYaw < 7) {
                vl++;
                if(vl > 20) {
                    flag("youre shit nibba");
                }
            } else vl-= vl > 0 ? 0.05 : 0;
            debug("sens=" + MovementProcessor.sensToPercent(data.moveProcessor.sensitivityX)
                    + ", " + MovementProcessor.sensToPercent(data.moveProcessor.sensitivityY)
                    + " deltaX=" + data.moveProcessor.deltaX + " deltaYaw=" + data.playerInfo.lDeltaYaw);
        }
    }

}
