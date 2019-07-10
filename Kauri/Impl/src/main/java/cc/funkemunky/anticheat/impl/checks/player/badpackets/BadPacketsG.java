package cc.funkemunky.anticheat.impl.checks.player.badpackets;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.anticheat.api.utils.StatisticalAnalysis;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.TickTimer;
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
    public float deltaBalance = 0.025f;

    @Setting(name = "threshold.vl.max")
    private int maxVL = 30;

    private long lastFlying;
    private int vl;
    private StatisticalAnalysis statisticalAnalysis = new StatisticalAnalysis(20);

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val data = this.getData();

        if (data.getLastLogin().hasNotPassed(15) || data.isServerPos()) {
            lastFlying = timeStamp;
            return;
        }

        if(!getData().isLagging() && timeStamp - lastFlying > 5) {
            this.statisticalAnalysis.addValue(timeStamp - lastFlying);
        } else {
            lastFlying = timeStamp;
            return;
        }

        val max = Math.sqrt((50 - (1000 / Kauri.getInstance().getTps())) + 50);
        val stdDev = this.statisticalAnalysis.getStdDev();

        if (!MathUtils.approxEquals(deltaBalance, max, stdDev) && !getData().isLagging() && stdDev < max && getData().getLastLag().hasPassed(10)) {
            if (vl++ > maxVL) {
                this.flag("S: " + stdDev, false, true, vl > 60 ? AlertTier.HIGH : AlertTier.LIKELY);
            }
        } else vl -= vl > 0 ? 3 : 0;

        debug("STD: " + stdDev + " VL: " + vl + " max=" + max);
        this.lastFlying = timeStamp;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
