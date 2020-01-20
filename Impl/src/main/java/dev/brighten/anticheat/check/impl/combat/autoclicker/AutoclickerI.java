package dev.brighten.anticheat.check.impl.combat.autoclicker;

import dev.brighten.anticheat.check.api.*;
import dev.brighten.anticheat.utils.EvictingList;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Autoclicker (I)", description = "Click inconsistencies autoclicker check.",
        checkType = CheckType.AUTOCLICKER, punishVL = 10)
@Cancellable(cancelType = CancelType.INTERACT)
public class AutoclickerI extends Check {

    private int ticks, cps, verbose;
    private final EvictingList<Integer> cpsSamples = new EvictingList<>(10);

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        if (++this.ticks == 20) {
            if (cps > 0 && cpsSamples.add(cps)) {
                double cpsAverage = cpsSamples.stream().mapToDouble(Integer::doubleValue).average().orElse(0.0);
                double ratio = cpsAverage / cps;

                if (ratio > 0.99 && cpsAverage > 7) {
                    if (++verbose > 4) {
                        vl++;
                        this.flag("r=%1 a=%2", ratio, cpsAverage);
                    }
                } else {
                    verbose = 0;
                    vl = Math.max(vl - 1, 0);
                }

                debug("ratio=%1 vl=%2 avg=%3", ratio, verbose, cpsAverage);
            }
            this.ticks = 0;
            this.cps = 0;
        }
    }


    @Packet
    public void onClick(WrappedInArmAnimationPacket packet, long timeStamp) {
        if(data.playerInfo.breakingBlock || data.playerInfo.lastBlockPlace.hasNotPassed(3)) return;

        this.cps++;
    }
}