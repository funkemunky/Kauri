package dev.brighten.anticheat.premium.impl.autoclicker;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInTransactionPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.MiscUtils;
import dev.brighten.api.KauriVersion;
import dev.brighten.api.check.CheckType;
import lombok.val;

@CheckInfo(name = "Autoclicker (F)", description = "Checks for a constant skew of values.", developer = true,
        checkType = CheckType.AUTOCLICKER, minVersion = ProtocolVersion.V1_8, planVersion = KauriVersion.ARA)
public class AutoclickerF extends Check {

    public int flying, lflying, buffer, attackTick;

    public EvictingList<Integer> hitAverage = new EvictingList<>(15), noHitAvg = new EvictingList<>(15);

    @Packet
    public void onFlying(WrappedInTransactionPacket packet) {
        val optional = Kauri.INSTANCE.keepaliveProcessor.getKeepById(packet.getAction());

        if(optional.isPresent()) {
            flying++;
        }
    }

    @Packet
    public void onUse(WrappedInUseEntityPacket packet, int current) {
        attackTick = current;
    }

    @Packet
    public void onArm(WrappedInArmAnimationPacket packet, int current) {
        if(data.playerInfo.breakingBlock
                || data.playerInfo.lookingAtBlock
                || data.playerInfo.lastBrokenBlock.isNotPassed(5)
                || data.playerInfo.lastBlockDigPacket.isNotPassed(1)
                || data.playerInfo.lastBlockPlacePacket.isNotPassed(1))
            return;
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

            double delta = Math.abs(nohit - hit);
            double std = MiscUtils.stdev(hitAverage), nstd = MiscUtils.stdev(noHitAvg);
            double stdDelta = Math.abs(std - nstd);

            if (delta < 0.05 && stdDelta < 0.08 && data.clickProcessor.getMean() <= 2) {
                vl++;
                flag("delta=%v.3 std=%v.3 mean=%v.1", delta, stdDelta, data.clickProcessor.getMean());
            }

            debug("flying=%v hit=%v.2 nohit=%v.2 std=%v nstd=%v", flying, hit, nohit, std, nstd);
        }
        lflying = flying;
        flying = 0;
    }
}
