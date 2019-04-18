package cc.funkemunky.anticheat.impl.checks.player.badpackets;

import cc.funkemunky.anticheat.Kauri;
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
        Packet.Client.LOOK,
        Packet.Client.LEGACY_POSITION,
        Packet.Client.LEGACY_POSITION_LOOK,
        Packet.Client.LEGACY_LOOK})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "BadPackets (Type G)", description = "Checks frequency of incoming packets. More detection, but less reliable.", type = CheckType.BADPACKETS, maxVL = 200, executable = false, developer = true)
public class BadPacketsG extends Check {

    @Setting(name = "usingPaperSpigot")
    public boolean usingPaper = false;

    @Setting(name = "lenience")
    public float deltaBalance = 0.02f;

    @Setting(name = "threshold.vl.max")
    private int maxVL = 30;

    private long lastFlying;
    private TickTimer lastLag = new TickTimer(5);
    private int vl;
    private StatisticalAnalysis statisticalAnalysis = new StatisticalAnalysis(20);

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val data = this.getData();

        if (data.getLastLogin().hasNotPassed(15) || data.getMovementProcessor().isServerPos()) {
            return;
        }

        if (MathUtils.getDelta(timeStamp, lastFlying) < 5) {
            lastLag.reset();
            statisticalAnalysis.addValue(50);
        } else if (lastLag.hasPassed(2)) {
            this.statisticalAnalysis.addValue(timeStamp - lastFlying);
        }

        val max = usingPaper ? 7.071f : Math.sqrt(Kauri.getInstance().getTickElapsed());
        val stdDev = this.statisticalAnalysis.getStdDev();

        if (!MathUtils.approxEquals(deltaBalance, max, stdDev) && stdDev < max && getData().getLastLag().hasNotPassed(10)) {
            if (lastLag.hasPassed() && vl++ > maxVL) {
                this.flag("S: " + stdDev, false, true);
            }
        } else vl -= vl > 0 ? 3 : 0;

        debug("STD: " + stdDev + " VL: " + vl);
        this.lastFlying = timeStamp;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
