package dev.brighten.anticheat.check.impl.combat.autoclicker;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockPlacePacket;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;

import java.util.List;

@CheckInfo(name = "Autoclicker (B)", description = "Checks for common blocking patterns",
        devStage = DevStage.ALPHA, checkType = CheckType.AUTOCLICKER, punishVL = 16)
@Cancellable(cancelType = CancelType.INTERACT)
public class AutoclickerB extends Check {

    private int lastPlace;
    private float buffer;
    private final List<Integer> tickDeltas = new EvictingList<>(15);

    @Packet
    public void onBlockPlace(WrappedInBlockPlacePacket packet, int currentTick) {
        if(data.playerInfo.breakingBlock || data.getPlayer().getItemInHand().getType().isBlock()) return;
        int deltaPlace = currentTick - lastPlace;

        tickDeltas.add(deltaPlace);
        if(tickDeltas.size() > 8) {
            int max = -10000000, min = Integer.MAX_VALUE;
            double average = 0;
            int range, total = 0;

            for (Integer delta : tickDeltas) {
                max = Math.max(delta, max);
                min = Math.min(delta, min);

                average+= delta;
                total++;
            }

            average/= total;
            range = max - min;

            if(average < 3 && range <= 1) {
                if(++buffer > 12) {
                    vl++;
                    flag("range=%s", range);
                }
            } else if(vl > 0) buffer-= 0.5f;

            debug("range=%s average=%.1f vl=%.1f", range, average, vl);
        }

        debug("deltaArm=%s", deltaPlace);
        lastPlace = currentTick;
    }
}
