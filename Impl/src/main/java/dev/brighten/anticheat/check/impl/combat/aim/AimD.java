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
    private double lstd;
    private int verbose;

    @Packet
    public void onPacket(WrappedInFlyingPacket packet) {
        if(packet.isLook() && data.target != null) {
            float[] angleArray = MathUtils.getRotations(data.playerInfo.to
                    .toLocation(data.getPlayer().getWorld()).add(0, data.playerInfo.sneaking ? 1.54 : 1.62, 0),
                    data.target.getLocation());

            float angle = MathUtils.getAngleDelta(angleArray[0] , data.playerInfo.to.yaw)
                    + Math.abs(angleArray[1] - data.playerInfo.to.pitch);

            interval.add(angle);

            if(interval.size() > 8) {
                val summary = interval.getSummary();
                val std = interval.std();
                val range = summary.getMax() - summary.getMin();

                if(MathUtils.getDelta(std, lstd) < 1 && range > 12) {
                    if(verbose++ > 2) {
                        vl++;
                        flag("avg=" + summary.getAverage() + " std=" + std);
                    }
                } else verbose-= verbose > 0 ? 0.5f : 0;
                debug("x= " + data.moveProcessor.deltaX
                        + " y=" + data.moveProcessor.deltaY
                        + " std=" + std
                        + " range=" + range
                        + " sens=" + data.moveProcessor.sensitivityX
                        + " verbose=" + verbose);

                interval.clear();
                lstd = std;
            }
        }
    }
}
