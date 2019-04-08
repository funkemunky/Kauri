package cc.funkemunky.anticheat.impl.checks.combat.autoclicker;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import lombok.val;
import org.bukkit.event.Event;

import java.util.Deque;
import java.util.LinkedList;

@CheckInfo(name = "Autoclicker (Type I)", description = "Something.", type = CheckType.AUTOCLICKER, cancelType = CancelType.INTERACT, developer = true)
//@Init
@Packets(packets = {Packet.Client.ARM_ANIMATION})
public class AutoclickerI extends Check {

    private long lastTimeStamp, lastMS, ms;

    private final Deque<Long> averageDeque = new LinkedList<>();

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val offset = 16777216L;

        lastMS = ms;
        ms = timeStamp - lastTimeStamp;

        double cps = 1000D/ ms, lastCPS = 1000D / lastMS;

        val gcd = MiscUtils.gcd((long) (cps * offset), (long) (lastCPS * offset));

        averageDeque.add(gcd);

        if (averageDeque.size() == 20) {
            val distinct = averageDeque.stream().distinct().count();
            val duplicates = averageDeque.size() - distinct;

            if(duplicates > 0) {

            }

            debug(Color.Green + "DIS: " + distinct + " DUP: " + duplicates);
            averageDeque.clear();
        }

        debug("GCD: " + gcd + " CPS: " + cps);

        lastTimeStamp = timeStamp;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
