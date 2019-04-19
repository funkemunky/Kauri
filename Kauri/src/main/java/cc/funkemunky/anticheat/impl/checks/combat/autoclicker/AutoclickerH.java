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

@Packets(packets = {Packet.Client.ARM_ANIMATION})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "Autoclicker (Type H)", description = "Looks for suspicious clicking averages compares to actual clicks.", type = CheckType.AUTOCLICKER, cancelType = CancelType.INTERACT, maxVL = 50, executable = false, developer = true)
public class AutoclickerH extends Check {

    private long lastSwing;
    private final Deque<Long> delays = new LinkedList<>();
    private int vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if (!MiscUtils.shouldReturnArmAnimation(getData())) {
            val now = timeStamp;

            val delay = now - lastSwing;

            //Changed this so this check doesn't just stop working when a player has a delay longer than a certain period.
            if (delay < 160L) {
                delays.add(delay);
            }

            //AimI removed your leveling system due to it seeming very unnecessary from debugging. Comment your reasoning so AimI can confirm. - funke
            if (delays.size() == 50) {
                //val level = new AtomicInteger(0);

                //delays.stream().filter(range -> range == 0L).forEach(range -> level.getAndIncrement()); //dont judge me

                val average = delays.stream().mapToLong(Long::longValue).average().orElse(0.0);
                val averageDelta = Math.abs(delay - average); // Why are you doing this? What is your thought process?

                //Removed level.get() <= 14
                if (averageDelta <= 10) {
                    //AimI cleaned up this area and completely changed the verbose system due to players being able to easily flag this.
                    // However, unlike autoclickers, players do not flag it consistently every single intervalTime. This should do until further testing
                    // proves this check to be invalid or needing fixing.
                    if (vl++ > 6) {
                        flag("AVG: " + average, true, true);
                    }
                } else {
                    vl = 0;
                }

                debug("VL: " + vl + "DELTA: " + averageDelta  + "AVG: " + average + " VL: " + vl);

                delays.clear();
            }
            this.lastSwing = now;
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
