package cc.funkemunky.anticheat.impl.checks.player.badpackets;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.EvictingList;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.MathUtils;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {
        Packet.Client.FLYING,
        Packet.Client.POSITION,
        Packet.Client.POSITION_LOOK,
        Packet.Client.LOOK})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "BadPackets (Type G)",
        description = "Checks frequency of incoming packets. More detection, but less reliable.",
        type = CheckType.BADPACKETS, maxVL = 200)
public class BadPacketsG extends Check {

    private long lastTS;
    private double vl;
    private EvictingList<Long> times = new EvictingList<>(15);

    @Setting(name = "verbose.max")
    private static int verboseVL = 80;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        long elapsed = timeStamp - lastTS;

        if(getData().getLastLogin().hasPassed(10) && !move.isServerPos()) {
            if(elapsed > 2) times.add(elapsed);

            double average = times.stream().mapToLong(val -> val).average().orElse(50.0);
            double ratio = 50 / average;
            double pct = ratio * 100;

            if((pct > 101) && (timeStamp - getData().getLastServerPosStamp() > 150L)
                    && !getData().isLagging()
                    && System.currentTimeMillis() - Kauri.getInstance().getLastTPS() < 150
                    && Kauri.getInstance().getTps() > 18.5) {
                if(vl++ > verboseVL) {
                    flag("pct=" + MathUtils.round(pct, 2) + "%, vl=" + vl,
                            true, true, AlertTier.HIGH);
                }
            } else vl-= vl > 0 ? 1.5 : 0;

            debug("pct=" + pct + ", vl=" + vl);
        }
        lastTS = timeStamp;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
