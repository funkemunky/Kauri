package dev.brighten.anticheat.premium.impl.autoclicker;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.MiscUtils;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Autoclicker (F)", description = "Checks for a constant skew of values.", developer = true,
        checkType = CheckType.AUTOCLICKER, minVersion = ProtocolVersion.V1_8)
public class AutoclickerF extends Check {

    public int flying, lflying, buffer, attackTick;

    public EvictingList<Integer> hitAverage = new EvictingList<>(15), noHitAvg = new EvictingList<>(15);

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        flying++;
    }

    @Packet
    public void onUse(WrappedInUseEntityPacket packet, int current) {
        attackTick = current;
    }

    @Packet
    public void onArm(WrappedInArmAnimationPacket packet, int current) {
        checkProcessing:
        {
            if (flying > 10)
                break checkProcessing;
            if (data.playerInfo.breakingBlock || current - attackTick < 4)
                hitAverage.add(flying);
            else noHitAvg.add(flying);

            if (hitAverage.size() < 14 || noHitAvg.size() < 14) return;

            double hit = hitAverage.stream().mapToInt(v -> v).average().orElse(-1),
                    nohit = noHitAvg.stream().mapToInt(v -> v).average().orElse(-1);

            double delta = nohit - hit;
            double std = MiscUtils.stdev(hitAverage), nstd = MiscUtils.stdev(noHitAvg);

            //We dont do Math.abs since it actually can hinder detection in this instance.
            //It should never go below zero if the player is legitimate.
            if (delta < 0.05 && Math.abs(std - nstd) < 0.08) {
                vl++;
                flag("shit");
            }

            debug("flying=%v hit=%v.2 nohit=%v.2 std=%v nstd=%v", flying, hit, nohit, std, nstd);
        }
        lflying = flying;
        flying = 0;
    }
}
