package dev.brighten.anticheat.premium.impl.autoclicker;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.utils.math.cond.MaxDouble;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import lombok.val;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

@CheckInfo(name = "Autoclicker (I)", description = "Checks for double clicking abuse.",
        checkType = CheckType.AUTOCLICKER, developer = true)
public class AutoclickerI extends Check {

    private int buffer = 0;

    @Packet
    public void onClick(WrappedInArmAnimationPacket packet) {

        if (data.playerInfo.breakingBlock
                || data.playerInfo.lastBlockPlace.hasNotPassed(1))
            return;

        if(data.clickProcessor.getZeros() > 1 && data.clickProcessor.getAverage() > 55) {
            if(++buffer > 12) {
                vl++;
                flag("doubleClicks=%v average=%v.2 buffer=%v",
                        data.clickProcessor.getZeros(), data.clickProcessor.getAverage(), buffer);
            }
        } else buffer = 0;

        debug("buffer=%v zeros=%v avg=%v.2",
                data.clickProcessor.getZeros(), data.clickProcessor.getAverage(), buffer);
    }
}
