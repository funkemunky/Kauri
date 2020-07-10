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
import dev.brighten.anticheat.utils.MiscUtils;
import dev.brighten.anticheat.utils.TickTimer;
import dev.brighten.api.check.CheckType;
import lombok.val;

import java.util.ArrayList;
import java.util.List;

@CheckInfo(name = "Timer", description = "Checks the rate of packets coming in.",
        checkType = CheckType.BADPACKETS, vlToFlag = 4, developer = true)
@Cancellable
public class Timer extends Check {

    private int ticks, buffer;
    private EvictingList<Integer> tickList = new EvictingList<>(60);

    @Packet
    public void onPacket(WrappedInFlyingPacket packet) {
        ticks++;
    }

    @Packet
    public void onTrans(WrappedInTransactionPacket packet, long current) {
        val optional = Kauri.INSTANCE.keepaliveProcessor.getKeepById(packet.getAction());

        if(optional.isPresent()) {
            tickList.add(ticks);
            if(current - data.creation > 2000L) {
                val average = tickList.stream().mapToInt(v -> v).average().orElse(1);
                if (average > 1.014) {
                    if (++buffer > 80) {
                        vl++;
                        flag("timer=%v.1% buffer=%v", average * 100, buffer);
                    }
                } else if (buffer > 0) buffer -= 2;
                debug("tick=%v avg=%v.3 buffer=%v", ticks, average, buffer);
            }
            ticks = 0;
        }
    }
}