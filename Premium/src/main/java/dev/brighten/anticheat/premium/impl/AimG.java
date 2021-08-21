package dev.brighten.anticheat.premium.impl;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.KauriVersion;
import dev.brighten.api.check.CheckType;;import java.util.List;

@CheckInfo(name = "Aim (G)", description = "gcd fix patching",
        checkType = CheckType.AIM, punishVL = 30, developer = true, planVersion = KauriVersion.ARA)
public class AimG extends Check {

    private int buffer;
    private List<Double> errors = new EvictingList<>(40);
    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(!packet.isLook()) return;

        double deltaYaw = data.playerInfo.from.yaw - data.playerInfo.to.yaw;

        float sens = data.moveProcessor.sensitivityX;
        float f = sens * 0.6F * 0.2F;
        float gcd = f * f * f * 1.2F;
        float f1 = data.moveProcessor.sensitivityY * 0.6F * 0.2F;
        float gcd1 = f1 * f1 * f1 * 1.2F;

        double error = Math.abs(deltaYaw % gcd), error1 = Math.abs(deltaYaw % gcd1);

        if(!Double.isNaN(error))
        errors.add(error);
        if(!Double.isNaN(error1))
        errors.add(error1);

        if(errors.size() > 10) {
            double std = MathUtils.stdev(errors);

            double delta = Math.abs(std - error);
            debug("delta=%s std=%s y=%s p=%s", delta, std, error, error1);
        }

    }
}