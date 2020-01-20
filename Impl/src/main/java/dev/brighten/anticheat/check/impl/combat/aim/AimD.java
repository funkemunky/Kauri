package dev.brighten.anticheat.check.impl.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.math.cond.MaxDouble;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Aim (D)", description = "Designed to detect Vape's aimassist.",
        checkType = CheckType.AIM, punishVL = 20, developer = true, executable = false)
public class AimD extends Check {

    private MaxDouble verbose = new MaxDouble(20);

    @Packet
    public void onPacket(WrappedInFlyingPacket packet) {
        if (packet.isLook()) {
            float deltaYDiff = Math.abs(data.playerInfo.deltaYaw - data.playerInfo.lDeltaYaw),
                    deltaPDiff = Math.abs(data.playerInfo.deltaPitch - data.playerInfo.lDeltaPitch),
                    deltaYaw = Math.abs(data.playerInfo.deltaYaw),
                    deltaPitch = Math.abs(data.playerInfo.deltaPitch);
            if(deltaYaw > 0 || data.moveProcessor.deltaY > 0) {
                if(deltaYaw > deltaYDiff
                        && deltaYDiff > 0
                        && data.moveProcessor.deltaX > 4
                        && data.moveProcessor.deltaY <= 3
                        && deltaPDiff > deltaPitch * 2) {
                    if(verbose.add() > 3) {
                        vl++;
                        flag("verbose=%1 deltaYaw=%2 deltaYDiff=%3 deltaPitch=%4 deltaPDiff=%5",
                                MathUtils.round(verbose.value(), 3),
                                MathUtils.round(deltaYaw, 4),
                                MathUtils.round(deltaYDiff, 4),
                                data.moveProcessor.deltaY,
                                deltaPDiff);
                    }
                } else verbose.subtract(0.1);
                debug("verbose=%1 deltaYaw=%2 deltaYDiff=%3 deltaPitch=%4 deltaPDiff=%5",
                        MathUtils.round(verbose.value(), 3),
                        MathUtils.round(deltaYaw, 4),
                        MathUtils.round(deltaYDiff, 4),
                        deltaPitch,
                        deltaPDiff);
            }
        }
    }
}
