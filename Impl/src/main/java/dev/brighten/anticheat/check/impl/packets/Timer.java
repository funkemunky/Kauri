package dev.brighten.anticheat.check.impl.packets;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInTransactionPacket;
import cc.funkemunky.api.utils.math.RollingAverage;
import cc.funkemunky.api.utils.math.RollingAverageLong;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import lombok.val;

import java.util.ArrayList;
import java.util.List;

@CheckInfo(name = "Timer", description = "Checks the rate of packets coming in.",
        checkType = CheckType.BADPACKETS, vlToFlag = 4, developer = true)
@Cancellable
public class Timer extends Check {

    private int ticks, buffer;
    private RollingAverage avg = new RollingAverage(80);

    @Packet
    public void onPacket(WrappedInFlyingPacket packet, long current) {
        avg.add(ticks, current);
        if(current - data.creation > 2000L) {
            val average = 1 + (1 - avg.getAverage());
            if (average > 1.011) {
                if (++buffer > 8) {
                    vl++;
                    flag("timer=%v.1% buffer=%v", average * 100, buffer);
                }
            } else if (buffer > 0) buffer -= 2;
            debug("tick=%v avg=%v.3 buffer=%v", ticks, average, buffer);
        }
        ticks = 0;
    }

    @Packet
    public void onTrans(WrappedInTransactionPacket packet) {
        val optional = Kauri.INSTANCE.keepaliveProcessor.getKeepById(packet.getAction());

        if(optional.isPresent()) {
            ticks++;
        }
    }
}