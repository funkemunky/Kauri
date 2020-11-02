package dev.brighten.anticheat.check.impl.combat.autoclicker;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockPlacePacket;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;
import lombok.val;

@CheckInfo(name = "Autoclicker (B)", description = "Checks for common blocking patterns",
        developer = true, checkType = CheckType.AUTOCLICKER, punishVL = 35)
@Cancellable(cancelType = CancelType.INTERACT)
public class AutoclickerB extends Check {

    private int lastPlace, lastArm;
    private EvictingList<Integer> tickDeltas = new EvictingList<>(10);

    @Packet
    public void onArm(WrappedInArmAnimationPacket packet, int currentTick) {
        lastArm = currentTick;
    }

    @Packet
    public void onBlockPlace(WrappedInBlockPlacePacket packet, int currentTick) {
        if(data.playerInfo.lookingAtBlock || data.playerInfo.breakingBlock) return;
        int deltaArm = currentTick - lastArm;

        tickDeltas.add(deltaArm);
        if(tickDeltas.size() > 8) {
            val summary = tickDeltas.stream().mapToInt(v -> v).summaryStatistics();

            int range = summary.getMax() - summary.getMin();
            double average = summary.getAverage();

            if(average < 2 && range <= 1) {
                if(++vl > 12) {
                    flag("range=%v", range);
                }
            } else if(vl > 0) vl-= 0.5f;

            debug("range=%v average=%v.1 vl=%v.1", range, average, vl);
        }

        debug("deltaArm=%v", deltaArm);
        lastPlace = currentTick;
    }
}
