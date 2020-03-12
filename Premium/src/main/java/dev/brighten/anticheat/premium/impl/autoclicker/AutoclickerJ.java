package dev.brighten.anticheat.premium.impl.autoclicker;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

import java.util.*;

@CheckInfo(name = "Autoclicker (J)", description = "Checks the kurtosis of a player's click pattern.",
        checkType = CheckType.AUTOCLICKER, developer = true)
public class AutoclickerJ extends Check {

    private List<Long> cpsList = new ArrayList<>();
    private long lastClick;

    @Packet
    public void onArm(WrappedInArmAnimationPacket packet, long timeStamp) {
        long delta = timeStamp - lastClick;

        cpsList.add(delta);

        if(cpsList.size() >= 40) {
            LongSummaryStatistics summary = cpsList.parallelStream().mapToLong(l -> l).summaryStatistics();
        }
        lastClick = timeStamp;
    }
}
