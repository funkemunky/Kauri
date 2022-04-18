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

@CheckInfo(name = "Timer (B)", description = "Checks the rate of packets coming in.",
        checkType = CheckType.BADPACKETS, vlToFlag = 5, punishVL = 12)
@Cancellable
public class TimerB extends Check {

    private long totalTimer = -1;
    private final Timer lastFlag = new MillisTimer(2000L),
            lastReset = new TickTimer();
    private int buffer;

    @Packet
    public void onTeleport(WrappedOutPositionPacket event) {
        totalTimer-= 50;
    }

    @Packet
    public void onBlockPlace(WrappedInBlockPlacePacket packet) {
        //In versions 1.17 and newer, players will send an extra flying when right clicking
        if(data.playerVersion.isOrAbove(ProtocolVersion.v1_17))
        totalTimer-= 50;
    }

    //Fixes the problem with 1.9 where PacketPlayInFlying isn't always sent by the client and it therefore
    //can infinitely never flag if a user was cheating on a 1.9+ client.
    @Packet
    public void onTransaction(WrappedInTransactionPacket packet, long now) {
        if(data.playerVersion.isBelow(ProtocolVersion.V1_9)) return;

        Kauri.INSTANCE.keepaliveProcessor.getKeepById(packet.getAction()).ifPresent(ka -> {
            long delta = now - ka.startStamp;

            //We want to make sure the player isn't lagging before we reset their timer.
            if(delta < 1095L && totalTimer - (now + 100) > 3000L) {
                totalTimer = now - 300;
            }
        });
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long now) {
        check: {
            //This means we haven't started counting
            if(totalTimer == -1) {
                totalTimer = data.creation - 50;
                debug("Set base time");
            }
            //Every flying should take 50ms to send in between. So for every flying, we add 50ms to the totalTime.
            else totalTimer+= 50;

            //Only players using timer will add enough to reach this threshold in theory.
            long threshold = now + 100, delta = totalTimer - threshold;

            boolean isLagProblem = (Kauri.INSTANCE.keepaliveProcessor.laggyPlayers
                    / (double)Kauri.INSTANCE.keepaliveProcessor.totalPlayers) > 0.8;

            if(totalTimer > threshold
                    //If most players on the server are lagging, it's very likely we have an unstable netty thread
                    //and therefore cannot rely on this detection.
                    && !isLagProblem) {
                if (++buffer > 4) {
                    vl++;
                    flag("p=%s;d=%s;r=%s", data.lagInfo.lastPacketDrop.getPassed(), delta, lastReset.getPassed());
                }
                totalTimer = now - 80;
                debug("Reset time");
                lastFlag.reset();
            } else if(lastFlag.isPassed(5000L)) buffer = 0;

            debug("d=%s, thr=%s, b=%s lp=%s cp=%s", delta, threshold, buffer,
                    isLagProblem, (data.lagInfo.lastPingDrop.isPassed(4)
                            && System.currentTimeMillis() - data.lagInfo.lastClientTrans < 120L));
        }
    }
}