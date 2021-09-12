package dev.brighten.anticheat.check.impl.packets.badpackets;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInKeepAlivePacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInTransactionPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutTransaction;
import cc.funkemunky.api.utils.Color;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.anticheat.utils.timer.Timer;
import dev.brighten.anticheat.utils.timer.impl.MillisTimer;
import dev.brighten.anticheat.utils.timer.impl.TickTimer;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;
import lombok.val;

@CheckInfo(name = "BadPackets (N)", description = "Designed to patch disablers for Kauri.",
        checkType = CheckType.BADPACKETS, punishVL = 50, vlToFlag = 4, executable = false)
@Cancellable(cancelType = CancelType.MOVEMENT)
public class BadPacketsN extends Check {
    @Setting(name  = "kickPlayer")
    private static boolean kickPlayer = true;

    private int flying, lastTick, noBuffer;
    private final Timer lastTrans = new TickTimer(), lastSentTrans = new MillisTimer(), lastKeepAlive = new TickTimer(),
            lastFlying = new TickTimer();

    @Setting(name = "strings.kick")
    private static String kickString = "[Kauri] Invalid packets (%s).";

    public BadPacketsN() {
        lastSentTrans.reset();
        lastTrans.reset();
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {

        if(lastSentTrans.isNotPassed(300L) && lastKeepAlive.isNotPassed(3000L)
                && Kauri.INSTANCE.tps.getAverage() > 19.6
                && ++flying > 200) {
            vl++;
            flag("f=%s lKA=%s t=CANCEL", flying, lastKeepAlive.getPassed());

            if(!isExecutable() && vl > 4) kickPlayer(String.format(Color.translate(kickString), "TN"));
        }

        lastFlying.reset();
    }

    @Packet
    public void onKeepalive(WrappedInKeepAlivePacket packet) {
        lastKeepAlive.reset();
    }

    @Packet
    public void onOutTrans(WrappedOutTransaction packet) {
        lastSentTrans.reset();
    }

    @Packet
    public void onTransaction(WrappedInTransactionPacket packet, long now) {
        if(packet.getId() != 0) return;

        val response
                = Kauri.INSTANCE.keepaliveProcessor.getKeepById(packet.getAction());

        if (response.isPresent()) {
            flying = 0;
            lastTrans.reset();
            int current = response.get().start;

            if (current - lastTick > 1 && lastTick != 0 && lastTick != 1 && now - data.creation > 4000L) {
                vl++;
                flag("c=%s last=%s d=%s t=SKIP", current, lastTick, current - lastTick);
            }
            debug("c=%s l=%s", current, lastTick);
            lastTick = current;
        }
    }
}
