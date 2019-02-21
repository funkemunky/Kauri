package cc.funkemunky.anticheat.impl.checks.combat.autoclicker;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import lombok.val;
import org.bukkit.event.Event;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;

@Packets(packets = {
        Packet.Client.ARM_ANIMATION,
        Packet.Client.FLYING,
        Packet.Client.POSITION,
        Packet.Client.POSITION_LOOK,
        Packet.Client.LOOK,
        Packet.Client.LEGACY_POSITION,
        Packet.Client.LEGACY_POSITION_LOOK,
        Packet.Client.LEGACY_LOOK})
public class AutoclickerB extends Check {

    private final Deque<Double> averageDeque = new LinkedList<>();
    private final int[] cps = new int[]{0, 0, 0, 0};
    private final double[] averages = new double[]{0.0, 0.0, 0.0, 0.0};
    private int ticks, vl;
    private long timestamp;
    public AutoclickerB(String name, CheckType type, CancelType cancelType, int maxVL) {
        super(name, type, cancelType, maxVL);
    }

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        switch (packetType) {
            case Packet.Client.FLYING:
            case Packet.Client.POSITION:
            case Packet.Client.POSITION_LOOK:
            case Packet.Client.LOOK:
            case Packet.Client.LEGACY_LOOK:
            case Packet.Client.LEGACY_POSITION:
            case Packet.Client.LEGACY_POSITION_LOOK: {
                ticks++;

                if (ticks == 20 && cps[3] > 4) { // 20 ticks = 1 second
                    val now = System.currentTimeMillis();

                    val cpsAverage = Arrays.stream(cps).average().orElse(0.0);
                    val avgAverage = Arrays.stream(averages).average().orElse(0.0);
                    averages[3] = cpsAverage;

                    val averageDiff = Math.round(Math.abs(cpsAverage - avgAverage) * 100.0) / 100.0;

                    if (averageDiff > 0.d && now - timestamp < 1000L) {
                        averageDeque.add(averageDiff);
                    }

                    if (averageDeque.size() == 5) {
                        val distinct = averageDeque.stream().distinct().count();
                        val duplicates = averageDeque.size() - distinct;

                        if (duplicates > 0) {
                            vl += duplicates;

                            if (vl > 3) {
                                this.flag("D: " + duplicates, false, false);
                            }
                        } else {
                            vl -= vl > 0 ? 1 : 0;
                        }

                        averageDeque.clear();
                    }

                    this.pass();
                    ticks = 0;
                }
                break;
            }

            case Packet.Client.ARM_ANIMATION: {
                cps[3] = cps[3] + 1;

                val now = System.currentTimeMillis();

                timestamp = now;
                break;
            }
        }
        return;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }

    private void pass() {
        cps[0] = cps[1];
        cps[1] = cps[2];
        cps[2] = cps[3];
        cps[3] = 0;

        averages[0] = averages[1];
        averages[1] = averages[2];
        averages[2] = averages[3];
        averages[3] = 0;
    }
}
