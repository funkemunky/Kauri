package dev.brighten.anticheat.check.impl.packets.badpackets;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInKeepAlivePacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInTransactionPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutTransaction;
import cc.funkemunky.api.utils.RunUtils;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.check.api.Setting;
import dev.brighten.anticheat.utils.timer.Timer;
import dev.brighten.anticheat.utils.timer.impl.TickTimer;
import dev.brighten.api.check.CheckType;
import lombok.val;
import org.bukkit.Bukkit;

import java.util.logging.Level;

@CheckInfo(name = "BadPackets (N)", description = "Designed to patch disablers for Kauri.",
        checkType = CheckType.BADPACKETS, punishVL = 50, vlToFlag = 4)
public class BadPacketsN extends Check {

    @Setting(name  = "kickPlayer")
    private static boolean kickPlayer = true;
    private int flying, lastTick;
    private final Timer lastKeepalive = new TickTimer(), lastSentTrans = new TickTimer();

    public BadPacketsN() {
        lastSentTrans.reset();
        lastKeepalive.reset();
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(lastSentTrans.isNotPassed(5) && ++flying > 20) {
            vl++;
            flag("f=%s t=FLYING", flying);

            if(vl > punishVl && kickPlayer && !isExecutable()) {
                kickPlayer("[Kauri] Invalid packets.");
            }
        }

        if(lastKeepalive.isPassed(90) && kickPlayer) {
            Bukkit.getLogger().log(Level.INFO, "Kicking player " + packet.getPlayer().getName()
                    + " for not receiving any keepalives");
            kickPlayer("Lost connection");
        }
    }

    @Packet
    public void onOut(WrappedOutTransaction packet) {
        lastSentTrans.reset();
    }

    @Packet
    public void onKeepalive(WrappedInKeepAlivePacket packet) {
        lastKeepalive.reset();
    }

    @Packet
    public void onTransaction(WrappedInTransactionPacket packet, long now) {
        flying = 0;

        val response
                = Kauri.INSTANCE.keepaliveProcessor.getKeepById(packet.getAction());

        if (response.isPresent()) {
            int current = response.get().start;

            if (current - lastTick > 1 && now - data.creation > 4000L) {
                vl++;
                flag("d=%s t=SKIP", current - lastTick);
            }
            lastTick = current;
        }
    }
}
