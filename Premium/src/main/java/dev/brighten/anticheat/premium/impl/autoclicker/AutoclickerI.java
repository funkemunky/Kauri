package dev.brighten.anticheat.premium.impl.autoclicker;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Autoclicker (I)", description = "Checks for impossible ratio consistency. Check by Elevated.",
        checkType = CheckType.AUTOCLICKER, enabled = false, developer = true)
public class AutoclickerI extends Check {

    private final EvictingList<Integer> cpsSamples = new EvictingList<>(10);

    private int cps, ticks, buffer;

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if (++ticks == 20) {
            // Making sure the player is clicking on a cps that is acceptable and isn't digging.
            if (cps > 0 && cpsSamples.add(cps)) {
                // Get the average cps and the ratio of the clicks
                final double cpsAverage = cpsSamples.stream().mapToDouble(Integer::doubleValue)
                        .average().orElse(0.0);
                final double ratio = cpsAverage / cps;

                // If the cps is > 8 (current) and the average is also >= 8
                if (cps >= 8 && cpsAverage >= 8) {
                    // Impossible ratio consistency
                    if (ratio > 0.99) {
                        if (++buffer > 5) {
                            vl++;
                            flag("ratio=%1", MathUtils.round(ratio, 3));
                        }
                    } else {
                        buffer = 0;
                    }
                }
                debug("ratio=%1 buffer=%2 cps=%3", ratio, buffer, cps);
            }
            this.ticks = cps = 0;
        }
    }

    @Packet
    public void onArm(WrappedInArmAnimationPacket packet) {
        if(!data.playerInfo.lookingAtBlock
                && !data.playerInfo.breakingBlock
                && data.playerInfo.lastBlockPlace.hasPassed(5))
            cps++;
    }
}
