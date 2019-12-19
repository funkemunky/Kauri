package dev.brighten.anticheat.check.impl.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.objects.Interval;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import lombok.val;

@CheckInfo(name = "Aim (D)", description = "Designed to detect aimassists attempting that use cinematic smoothing.",
        checkType = CheckType.AIM, punishVL = 20, developer = true, executable = false)
public class AimD extends Check {

    private Interval interval = new Interval(20);
    private long ldeltax;

    @Packet
    public void onPacket(WrappedInFlyingPacket packet) {
        if(packet.isLook()) {
            boolean cinematic = data.playerInfo.cinematicModePitch || data.playerInfo.cinematicModeYaw;
            boolean goodToGo = data.moveProcessor.yawGcdList.size() > 30;

            if(goodToGo) {
                interval.add(data.moveProcessor.deltaX);
                double avg = interval.average();
                double std = interval.std();

                if(MathUtils.getDelta(avg, data.moveProcessor.deltaX) < 15
                        && MathUtils.getDelta(ldeltax, data.moveProcessor.deltaX) < 5
                        && data.playerInfo.deltaY <= 1
                        && data.moveProcessor.deltaX > 10) {
                    if(vl++ > 5) {
                        flag("shit");
                    }
                } else vl-= vl > 0 ? 1 : 0;
                debug("deltaX=" + data.moveProcessor.deltaX + " avg=" + avg + " std=" + std + " vl=" + vl);
                ldeltax = data.moveProcessor.deltaX;
            }
        }
    }
}