package cc.funkemunky.anticheat.impl.checks.player.badpackets;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Interval;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {
        Packet.Client.FLYING,
        Packet.Client.POSITION,
        Packet.Client.POSITION_LOOK,
        Packet.Client.LOOK})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "BadPackets (Type G)", description = "Checks frequency of incoming packets. More detection, but less reliable.", type = CheckType.BADPACKETS, maxVL = 200, executable = false)
public class BadPacketsG extends Check {

    @Setting(name = "usingPaperSpigot")
    public boolean usingPaper = false;

    @Setting(name = "lenience")
    public float deltaBalance = 0.04f;

    @Setting(name = "threshold.vl.max")
    private int maxVL = 50;

    private long lastFlying;
    private double vl;
    private Interval<Long> interval = new Interval<>(0, 20);

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();
        if (getData().getLastLogin().hasNotPassed(15) || move.isServerPos()) {
            lastFlying = timeStamp;
            return;
        }

        if(!getData().isLagging() && timeStamp - lastFlying > 5) {
            this.interval.add(timeStamp - lastFlying);
        } else {
            lastFlying = timeStamp;
            return;
        }

        double avg = interval.average();
        double pct = (1000 / Kauri.getInstance().getTps()) / avg * 100;

        if(pct > 100.2) {
            if(vl++ > 20) {
                flag("pct=" + pct + "%", true, true, vl > 30 ? AlertTier.HIGH : AlertTier.LIKELY);
            }
        } else vl-= vl > 0 ? 2 : 0;

        debug("avg=" + avg + " pct=" + pct + "%" + " vl=" + vl);
        this.lastFlying = timeStamp;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
