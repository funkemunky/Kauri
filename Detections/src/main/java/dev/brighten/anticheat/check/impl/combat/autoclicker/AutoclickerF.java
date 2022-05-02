package dev.brighten.anticheat.check.impl.combat.autoclicker;

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
import dev.brighten.anticheat.utils.SimpleAverage;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;
import lombok.val;

import java.util.List;

@CheckInfo(name = "Autoclicker (F)", description = "Looks for consistency between attack hits and miss hits.",
        devStage = DevStage.ALPHA,
        checkType = CheckType.AUTOCLICKER, minVersion = ProtocolVersion.V1_8)
public class AutoclickerF extends Check {

    public int flying, lflying, buffer, attackTick;

    public SimpleAverage hitAverage = new SimpleAverage(20,0), noHitAvg = new SimpleAverage(20, 0);
    public List<Integer> hitList = new EvictingList<>(20), noHitLit = new EvictingList<>(20);

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
            if (data.playerInfo.breakingBlock || current - attackTick < 4) {
                hitAverage.add(flying);
                hitList.add(flying);
            }
            else {
                noHitAvg.add(flying);
                noHitLit.add(flying);
            }

            if (hitAverage.getSize() < 14 || noHitAvg.getSize() < 14) break checkProcessing;

            double hit = hitAverage.getAverage(),
                    nohit = noHitAvg.getAverage();

            double delta = Math.abs(nohit - hit);
            double std = MiscUtils.stdev(hitList), nstd = MiscUtils.stdev(noHitLit);
            double stdDelta = Math.abs(std - nstd);

            if (delta < 0.05 && stdDelta < 0.08 && data.clickProcessor.getMean() <= 2) {
                vl++;
                flag("delta=%.3f std=%.3f mean=%.1f", delta, stdDelta, data.clickProcessor.getMean());
            }

            debug("flying=%s hit=%.2f nohit=%.2f std=%s nstd=%s", flying, hit, nohit, std, nstd);
        }
        lflying = flying;
        flying = 0;
    }
}
