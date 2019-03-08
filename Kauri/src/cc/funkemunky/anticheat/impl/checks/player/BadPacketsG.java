package cc.funkemunky.anticheat.impl.checks.player;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.anticheat.api.utils.StatisticalAnalysis;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.MathUtils;
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
public class BadPacketsG extends Check {

    public BadPacketsG(String name, String description, CheckType type, CancelType cancelType, int maxVL, boolean enabled, boolean executable, boolean cancellable) {
        super(name, description, type, cancelType, maxVL, enabled, executable, cancellable);

        setDeveloper(true);
    }

    @Setting(name = "usingPaperSpigot")
    public boolean usingPaper = false;

    @Setting(name = "lenience")
    public float deltaBalance = 0.02f;

    @Setting(name = "threshold.vl.max")
    private int maxVL = 14;

    private long lastFlying;
    private int vl;
    private StatisticalAnalysis statisticalAnalysis = new StatisticalAnalysis(20);

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(timeStamp > lastFlying + 5) {
            val data = this.getData();

            if (data.getLastLogin().hasNotPassed(15) || data.getLastServerPos().hasNotPassed(5)) {
                return;
            }

            this.statisticalAnalysis.addValue(timeStamp - lastFlying);

            val max = usingPaper ? 7.071f : Math.sqrt(Kauri.getInstance().getTickElapsed());
            val stdDev = this.statisticalAnalysis.getStdDev();

            if (!MathUtils.approxEquals(deltaBalance, max, stdDev) && stdDev < max && !data.isLagging()) {
                if(vl++ > maxVL) {
                    this.flag("S: " + stdDev, false, true);
                }
            } else vl -= vl > 0 ? 2 : 0;
        }

        this.lastFlying = timeStamp;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
