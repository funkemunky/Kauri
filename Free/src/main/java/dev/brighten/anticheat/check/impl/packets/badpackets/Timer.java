package dev.brighten.anticheat.check.impl.packets.badpackets;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInTransactionPacket;
import cc.funkemunky.api.utils.math.RollingAverage;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.SimpleAverage;
import dev.brighten.api.KauriVersion;
import dev.brighten.api.check.CheckType;
import lombok.val;

/*
 * This check is designed to be impervious to lag spikes and concurrency problems. For every flying packet coming in
 * we add 50ms to our total time, starting when they initially start sending PacketPlayInFlying. We set our threshold
 * to the current time with the highest millisecond average (usually above 50) they had created. This is to prevent
 * sudden concurrency problems from lag spikes causing a false positive. If a player starts adding time above this
 * threshold, then their game must be sending packets faster than possible and therefore we can conclude they are
 * using Timer.
 */
@CheckInfo(name = "Timer (A)", description = "Checks the rate of packets coming in.",
        checkType = CheckType.BADPACKETS, vlToFlag = 4, developer = true, planVersion = KauriVersion.FREE)
@Cancellable
public class Timer extends Check {

    private int buffer;
    private long maxLag = 50, lastFlying, totalFlying = -1L;
    private SimpleAverage timeAverage = new SimpleAverage(50, 50);

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long now) {
        long delta = now - lastFlying;

        if(!data.playerInfo.doingTeleport && data.playerInfo.lastTeleportTimer.isPassed(1)) {
            //Adding flying count
            if(totalFlying == -1L) totalFlying = now - 50L;
            else totalFlying+= 50L;

            boolean lagging = delta > 80 || delta < 20;

            if(delta < 3000L) //We do not want giant values messing up our average, especially ones from inital login
            timeAverage.add(delta);

            double avg = timeAverage.getAverage();
            long roundedAvg = Math.round(avg); //We will use this in areas where we need to set long values.

            //Preventing large maxAverages causing unnecessary timer bypass.
            if(!lagging && maxLag > 80) maxLag = roundedAvg;

            //Also preventing unnecessary runaway bypassing of Timer
            if(now - totalFlying > 500) totalFlying = now - 100;

            maxLag = Math.max(maxLag, roundedAvg);

            long threshold = now + maxLag + 50; //The extra 50 is a just in case

            //We are now checking if their total time is above our threshod
            if(totalFlying > threshold) {
                vl++;
                flag("[+%v]: %v, %v", totalFlying - threshold, totalFlying, threshold);

                totalFlying = now - 80; //Just preventing runaway flagging.
            }

            debug("time=%v threshold=%v", totalFlying, threshold);
        }

        lastFlying = now;
    }
}