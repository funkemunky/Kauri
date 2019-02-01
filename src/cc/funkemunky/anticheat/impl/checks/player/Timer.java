package cc.funkemunky.anticheat.impl.checks.player;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.StatisticalAnalysis;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {
        Packet.Client.LOOK,
        Packet.Client.POSITION_LOOK,
        Packet.Client.LEGACY_POSITION_LOOK,
        Packet.Client.FLYING,
        Packet.Client.LEGACY_LOOK})
public class Timer extends Check {

    public Timer(String name, CheckType type, CancelType cancelType, int maxVL) {
        super(name, type, cancelType, maxVL);
    }

    private long lastFlying;
    private StatisticalAnalysis statisticalAnalysis = new StatisticalAnalysis(20);

    @Override
    public Object onPacket(Object packet, String packetType, long timeStamp) {
        if (Packet.isPositionLook(packetType) || Packet.isPosition(packetType) || Packet.isLook(packetType) || packet instanceof WrappedInFlyingPacket) {
            val now = System.currentTimeMillis();

            val data = this.getData();

            if (data.isLagging() || data.getLastLogin().hasNotPassed(9) || data.getLastServerPos().hasNotPassed(9)) {
                return packet;
            }

            this.statisticalAnalysis.addValue(now - lastFlying);

            val max = 7.07;
            val stdDev = this.statisticalAnalysis.getStdDev(max);

            if (stdDev != 0.00E0 / 0.00E0 && stdDev < max) {
                this.flag("S: " + stdDev, false, true);
            }

            this.lastFlying = now;
        }
        return packet;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
