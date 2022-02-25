package dev.brighten.anticheat.check.impl.regular.packets;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutPositionPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

import java.util.concurrent.TimeUnit;

@CheckInfo(name = "Timer (C)", checkType = CheckType.BADPACKETS, enabled = true)
public class TimerC extends Check {

    private long lastNanos = -1L;
    private long balance;

    private double buffer;
    private long lastFlag;

    private final long bufferReset = TimeUnit.SECONDS.toMillis(45);

    @Packet
    public void teleport(WrappedOutPositionPacket wrappedOutPositionPacket) {
        this.balance -= TimeUnit.MILLISECONDS.toNanos(
                250L + data.lagInfo.transPing*50L
        );
    }

    @Packet
    public void onPacket(WrappedInFlyingPacket packet, long now) {
        long nanos = System.nanoTime();
        long lastDelta = (nanos - this.lastNanos);

        long delay = (50000000L - lastDelta);

        // cope about it
        if (!data.playerVersion.isBelow(ProtocolVersion.V1_8)) {
            long toMillis = TimeUnit.NANOSECONDS.toMillis(Math.abs(this.balance));

            if (toMillis > 3000L) {
                this.balance = (long) -3e+9;
            }
        }

        if (this.lastNanos > -1L) {
            this.balance += delay;

            if (this.balance > 45000000L && data.playerInfo.moveTicks > 60) {

                if (this.buffer++ > 7) {
                    this.flag(
                            "buffer=" + this.buffer,
                            "balance=" + this.balance
                    );
                }

                this.lastFlag = now;
                this.balance = 0;
            }

            if ((now - this.lastFlag) > this.bufferReset) {
                this.buffer -= this.buffer > 0 ? .1 : 0;
            }
        }

        this.lastNanos = nanos;
    }
}