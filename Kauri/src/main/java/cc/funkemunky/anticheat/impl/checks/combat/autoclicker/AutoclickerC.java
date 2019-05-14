package cc.funkemunky.anticheat.impl.checks.combat.autoclicker;

import cc.funkemunky.anticheat.api.checks.*;
import cc.funkemunky.anticheat.api.utils.DynamicRollingAverage;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {
        Packet.Client.ARM_ANIMATION,
        Packet.Client.FLYING,
        Packet.Client.POSITION,
        Packet.Client.POSITION_LOOK,
        Packet.Client.LOOK,
        Packet.Client.LEGACY_POSITION,
        Packet.Client.LEGACY_POSITION_LOOK,
        Packet.Client.LEGACY_LOOK})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "Autoclicker (Type C)", description = "An overall average CPS check.", type = CheckType.AUTOCLICKER, cancelType = CancelType.BREAK, maxVL = 20, executable = false)
public class AutoclickerC extends Check {


    private final DynamicRollingAverage cpsAverage = new DynamicRollingAverage(5);
    private int cps, ticks;
    private double vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if (packetType.contains("Position") || packetType.contains("Look") || packetType.equals(Packet.Client.FLYING)) {
            if (++ticks == 20) {
                if (cps > 0) {
                    cpsAverage.add(cps);

                    val average = cpsAverage.getAverage();

                    if (average >= 9.0) {
                        if (Math.round(average) == average || Math.round(average) == average - 0.5) {
                            if (++vl > 10) {
                                flag(average + " -> " + (double) Math.round(average) + " -> " + "0.0", false, true, AlertTier.LIKELY);
                            }
                        } else {
                            vl -= vl > 0 ? 1 : 0;
                        }
                    } else {
                        vl -= vl > 0 ? 0.25 : 0;
                    }

                    if (cpsAverage.isReachedSize()) {
                        cpsAverage.clearValues();
                    }
                }

                cps = 0;
                ticks = 0;

                debug("AV: " + cpsAverage.getAverage() + " VL: " + vl);
            }
        } else if (!MiscUtils.shouldReturnArmAnimation(getData())) {
            cps++;
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
