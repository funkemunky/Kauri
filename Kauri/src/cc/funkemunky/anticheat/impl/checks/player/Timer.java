package cc.funkemunky.anticheat.impl.checks.player;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.anticheat.api.utils.StatisticalAnalysis;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
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
public class Timer extends Check {

    public Timer(String name, String description, CheckType type, CancelType cancelType, int maxVL, boolean enabled, boolean executable, boolean cancellable) {
        super(name, description, type, cancelType, maxVL, enabled, executable, cancellable);
    }

    @Setting(name = "usingPaperSpigot")
    public boolean usingPaper = false;

    @Setting(name = "lenience")
    public float deltaBalance = 0.02f;

    private long lastFlying;
    private int vl;
    private StatisticalAnalysis statisticalAnalysis = new StatisticalAnalysis(20);

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if (Packet.isPositionLook(packetType) || Packet.isPosition(packetType) || Packet.isLook(packetType) || packet instanceof WrappedInFlyingPacket) {
            val now = System.currentTimeMillis();

            val data = this.getData();

            if (data.isLagging() || data.getLastLogin().hasNotPassed(9) || data.getLastServerPos().hasNotPassed(9)) {
                return;
            }

            this.statisticalAnalysis.addValue(now - lastFlying);

            val max = usingPaper ? 7.071f : Math.sqrt(Kauri.getInstance().getTickElapsed());
            val stdDev = this.statisticalAnalysis.getStdDev();

            if (!MathUtils.approxEquals(deltaBalance, max, stdDev) && stdDev < max) {
                if(vl++ > 9) {
                    this.flag("S: " + stdDev, false, true);
                }
            } else vl -= vl > 0 ? 2 : 0;

            this.lastFlying = now;
        }
        return;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
