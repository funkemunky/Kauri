package dev.brighten.anticheat.check.impl.regular.packets;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockPlacePacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInTransactionPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutPositionPacket;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.timer.Timer;
import dev.brighten.anticheat.utils.timer.impl.MillisTimer;
import dev.brighten.anticheat.utils.timer.impl.TickTimer;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;

@CheckInfo(name = "Timer (B)", description = "Checks the rate of packets coming in.",
        checkType = CheckType.BADPACKETS, vlToFlag = 5, punishVL = 20)
@Cancellable
public class TimerB extends Check {

    private long totalTimer = -1;
    private final Timer lastFlag = new MillisTimer(2000L);
    private int buffer;
    private boolean flying;

    @Packet
    public void onTeleport(WrappedOutPositionPacket event) {
        totalTimer-= 50;
    }

    @Packet
    public void onBlockPlace(WrappedInBlockPlacePacket packet) {
        if(data.playerVersion.isOrAbove(ProtocolVersion.v1_17))
        totalTimer-= 50;
    }

    @Packet
    public void onTrans(WrappedInTransactionPacket packet) {
        if(totalTimer == -1) {
            totalTimer = System.currentTimeMillis() - 50;
            debug("Reset time");
        } else if(!flying && data.playerInfo.lastFlyingTimer.isPassed(1))
            totalTimer+= 50;
        flying = false;
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long now) {
        flying = true;
        if(totalTimer == -1) {
            totalTimer = now - 50;
            debug("Reset time");
        }
        else totalTimer+= 50;

        long threshold = now + 100, delta = totalTimer - threshold;

        boolean isLagProblem = (Kauri.INSTANCE.keepaliveProcessor.laggyPlayers
                / (double)Kauri.INSTANCE.keepaliveProcessor.totalPlayers) > 0.8;

        if(totalTimer > threshold) {
            if (++buffer > 4) {
                vl++;
                flag("p=%s;d=%s", data.lagInfo.lastPacketDrop.getPassed(), delta);
            }
            totalTimer = now - 50;
            debug("Reset time");
            lastFlag.reset();
        } else if(lastFlag.isPassed(5000L)) buffer = 0;

        debug("d=%s, thr=%s, b=%s lp=%s cp=%s", delta, threshold, buffer,
                isLagProblem, (data.lagInfo.lastPingDrop.isPassed(4) && System.currentTimeMillis() - data.lagInfo.lastClientTrans < 120L));
    }
}