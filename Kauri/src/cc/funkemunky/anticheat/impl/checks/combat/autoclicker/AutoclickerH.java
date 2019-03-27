package cc.funkemunky.anticheat.impl.checks.combat.autoclicker;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import lombok.val;
import org.bukkit.event.Event;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

@Packets(packets = {Packet.Client.ARM_ANIMATION})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "Autoclicker (Type H)", description = "Looks for suspicious clicking averages compares to actual clicks.", type = CheckType.AUTOCLICKER, cancelType = CancelType.INTERACT, maxVL = 50, executable = false)
public class AutoclickerH extends Check {

    public AutoclickerH() {

    }

    private long lastSwing;
    private final Deque<Long> delays = new LinkedList<>();
    private int vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if (!MiscUtils.shouldReturnArmAnimation(getData())) {
            val now = System.currentTimeMillis();

            val delay = now - lastSwing;

            if (delay > 160L) return;

            delays.add(delay);

            if (delays.size() == 50) {
                val level = new AtomicInteger(0);

                delays.stream().filter(range -> range == 0L).forEach(range -> level.getAndIncrement()); //dont judge me

                val average = delays.stream().mapToLong(Long::longValue).average().orElse(0.0);
                val averageDelta = Math.abs(delay - average);

                if (averageDelta <= 10 && level.get() <= 14) {
                    vl += 4;

                    if (vl > 6) {
                        this.debug("FLAGGED TYPE H");
                    }
                } else {
                    vl = Math.max(vl - 1, 0);
                }

                debug("AVG: " + average + " LEVEL: " + level.get() + " VL: " + vl);

                delays.clear();
            }

            this.lastSwing = now;
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
