package dev.brighten.anticheat.check.impl.combat.autoclicker;

import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.EvictingList;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Autoclicker (I)", description = "Click inconsistencies autoclicker check.",
        checkType = CheckType.AUTOCLICKER, punishVL = 10)
public class AutoclickerI extends Check {

    private int ticks, cps, verbose;
    private final EvictingList<Integer> cpsSamples = new EvictingList<>(10);

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        if (++this.ticks == 20) {
            if (cps > 0 && cpsSamples.add(cps)) {
                double cpsAverage = cpsSamples.stream().mapToDouble(Integer::doubleValue).average().orElse(0.0);
                double ratio = cpsAverage / cps;

                if (ratio > 0.99) {
                    if (++verbose > 3) {
                        vl++;
                        this.flag(ratio + "r");
                    }
                } else {
                    verbose = 0;
                }

                debug("ratio=%1 vl=%2", ratio, verbose);
            }
            this.ticks = 0;
            this.cps = 0;
        }
    }


    @Packet
    public void onClick(WrappedInArmAnimationPacket packet, long timeStamp) {
        this.cps++;
    }
}