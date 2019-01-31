package cc.funkemunky.anticheat.impl.checks.player;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.StatisticalAnalysis;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {Packet.Client.FLYING,
        Packet.Client.POSITION,
        Packet.Client.POSITION_LOOK,
        Packet.Client.LOOK,
        Packet.Client.LEGACY_POSITION,
        Packet.Client.LEGACY_POSITION_LOOK,
        Packet.Client.LEGACY_LOOK})
public class TimerB extends Check {

    private final StatisticalAnalysis movingStats = new StatisticalAnalysis(20);
    private long timestamp;
    private int vl;

    public TimerB(String name, CancelType cancelType, int maxVL) {
        super(name, cancelType, maxVL);
    }

    @Override
    public Object onPacket(Object packet, String packetType, long timeStamp) {
        switch (packetType) {
            case Packet.Client.FLYING:
            case Packet.Client.LEGACY_LOOK:
            case Packet.Client.LEGACY_POSITION:
            case Packet.Client.LEGACY_POSITION_LOOK:
            case Packet.Client.POSITION:
            case Packet.Client.POSITION_LOOK:
            case Packet.Client.LOOK: {
                val flyingDifference = timeStamp - this.timestamp;

                movingStats.addValue(flyingDifference);

                val deviationDifference = Math.abs(Math.sqrt(flyingDifference) - Math.sqrt(7.071));

                if (getData().getLastLogin().hasPassed(20) && !getData().isLagging())

                    if (deviationDifference > 0.99999F) {
                        if (++vl > 20) {
                            flag("D: " + deviationDifference, false, false);
                        }
                    } else {
                        vl = Math.max(vl - 5, 0);
                    }

                val max = Math.sqrt(Kauri.getInstance().getTickElapsed());

                this.timestamp = timeStamp;
                break;
            }
        }
        return packet;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
