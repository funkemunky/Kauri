package cc.funkemunky.anticheat.impl.checks.combat.autoclicker;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckType;
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
public class AutoclickerC extends Check {


    private final DynamicRollingAverage cpsAverage = new DynamicRollingAverage(5);
    private int cps, ticks, vl;
    public AutoclickerC(String name, CheckType type, CancelType cancelType, int maxVL) {
        super(name, type, cancelType, maxVL);
    }

    /*
        @Override
    public void onEvent(Event event) {
        if (event instanceof SwingPacketEvent) {
            cps++;
        } else if (event instanceof FlyingPacketEvent) {
            if (++ticks == 20) {

                if (data.getAttributeHandler().getRange() < 130L && cps > 0) {
                    cpsAverage.add(cps);

                    val average = cpsAverage.getAverage();

                    if (average >= 9.0) {
                        if (Math.round(average) == average || Math.round(average) == average % 0.5) {
                            if (++vl > 2) {
                                onViolation("failed " + name + CC.GRAY + " [" + average + " -> " + (double) Math.round(average) + " -> " + "0.0" + "]");
                            }
                        } else {
                            vl = Math.max(vl - 2, 0);
                        }
                    }

                    if (cpsAverage.isReachedSize()) {
                        cpsAverage.clearValues();
                    }
                }

                ticks = 0;
                cps = 0;
            }
        }
    }
}
     */

    @Override
    public Object onPacket(Object packet, String packetType, long timeStamp) {
        switch (packetType) {
            case Packet.Client.FLYING:
            case Packet.Client.POSITION:
            case Packet.Client.POSITION_LOOK:
            case Packet.Client.LOOK:
            case Packet.Client.LEGACY_LOOK:
            case Packet.Client.LEGACY_POSITION:
            case Packet.Client.LEGACY_POSITION_LOOK: {
                if (++ticks == 20) {
                    if (cps > 0) {
                        cpsAverage.add(cps);

                        val average = cpsAverage.getAverage();

                        if (average >= 9.0) {
                            if (Math.round(average) == average || Math.round(average) == average - 0.5) {
                                if (++vl > 4) {
                                    flag(average + " -> " + (double) Math.round(average) + " -> " + "0.0", false, false);
                                }
                            } else {
                                vl -= vl > 0 ? 2 : 0;
                            }
                        }

                        if (cpsAverage.isReachedSize()) {
                            cpsAverage.clearValues();
                        }
                    }

                    cps = 0;
                    ticks = 0;

                    debug("AV: " + cpsAverage.getAverage() + " VL: " + vl);
                }
                break;
            }

            case Packet.Client.ARM_ANIMATION: {
                if (MiscUtils.shouldReturnArmAnimation(getData())) return packet;
                cps++;
                break;
            }
        }
        return packet;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
