package cc.funkemunky.anticheat.impl.checks.combat.autoclicker;

import cc.funkemunky.anticheat.api.checks.*;
import cc.funkemunky.anticheat.api.utils.Interval;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import lombok.val;
import org.bukkit.event.Event;


@Packets(packets = {Packet.Client.FLYING,
        Packet.Client.POSITION,
        Packet.Client.POSITION_LOOK,
        Packet.Client.LOOK,
        Packet.Client.ARM_ANIMATION})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "Autoclicker (Type C)", description = "Checks for very common autoclicker mistakes.", type = CheckType.AUTOCLICKER, cancelType = CancelType.INTERACT, executable = false, maxVL = 20)
public class AutoclickerC extends Check {

    private int cps, ticks, vl;
    private Interval fraction = new Interval(0, 5);

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if (packetType.contains("Position") || packetType.contains("Look") || packetType.equals(Packet.Client.FLYING)) {
            if (MiscUtils.shouldReturnArmAnimation(getData())) return;
            if (++ticks == 20) {
                if (cps > 0) {
                    fraction.add(cps);

                    val maxCps = fraction.max();
                    val minCps = fraction.min();

                    val averageCps = fraction.average();

                    if (averageCps >= 8.0 && averageCps < 17 && maxCps == minCps && getData().getMovementProcessor().getLagTicks() == 0) {
                        if ((vl += 2) > 12.0) {
                            flag("t: " + vl, true, true, AlertTier.LIKELY);
                            getData().getTypeD().flag(5, 8000L);
                        } else if(vl > 7) {
                            flag("t: " + vl, true, false, AlertTier.POSSIBLE);
                            getData().getTypeD().flag(5, 8000L);
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
        } else if (!MiscUtils.shouldReturnArmAnimation(getData())) {
            ++cps;
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
