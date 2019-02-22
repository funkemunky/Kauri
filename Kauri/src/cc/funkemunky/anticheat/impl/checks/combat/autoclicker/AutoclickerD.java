package cc.funkemunky.anticheat.impl.checks.combat.autoclicker;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Interval;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import lombok.val;
import org.bukkit.event.Event;


@Packets(packets = {Packet.Client.FLYING,
        Packet.Client.POSITION,
        Packet.Client.POSITION_LOOK,
        Packet.Client.LOOK,
        Packet.Client.LEGACY_POSITION,
        Packet.Client.LEGACY_POSITION_LOOK,
        Packet.Client.LEGACY_LOOK,
        Packet.Client.ARM_ANIMATION})
public class AutoclickerD extends Check {
    public AutoclickerD(String name, CheckType type, CancelType cancelType, int maxVL) {
        super(name, type, cancelType, maxVL);
    }

    private int cps, ticks, vl;
    private Interval fraction = new Interval(0, 5);

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if (packetType.contains("Position") || packetType.contains("Look") || packetType.equals(Packet.Client.FLYING)) {
            if (++ticks == 20) {
                if (cps > 0) {
                    fraction.add(cps);

                    val maxCps = fraction.max();
                    val minCps = fraction.min();

                    val averageCps = fraction.average();

                    if (averageCps >= 8.0 && maxCps == minCps) {
                        if ((vl += 2) >= 6.0) {
                            flag("t: " + vl, true, true);
                        }
                    } else {
                        vl = Math.max(vl - 1, 0);
                    }

                    debug("AVERAGE: " + averageCps + " VL: " + vl + " MAX: " + maxCps + " MIN: " + minCps);
                }

                ticks = 0;
                cps = 0;
                fraction.clearIfMax();
            }
        } else {
            ++cps;
        }
        return;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
