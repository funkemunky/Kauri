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

import java.util.LinkedList;

@CheckInfo(name = "Autoclicker (Type J)", description = "test", type = CheckType.AUTOCLICKER, cancelType = CancelType.INTERACT)
//@Init
@Packets(packets = {Packet.Client.ARM_ANIMATION})
public class AutoclickerJ extends Check {

    private long lastTimeStamp;
    private LinkedList<Long> list = new LinkedList<>();

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(MiscUtils.shouldReturnArmAnimation(getData())) return;

        long ms = timeStamp - lastTimeStamp;

        if(list.size() >= 20) {
            val average = (float) list.stream().mapToLong(l -> l).average().getAsDouble();
            val range = list.stream().mapToLong(l -> l).max().getAsLong() - list.stream().mapToLong(l -> l).min().getAsLong();
            debug("first=" + list.getFirst() + " last=" + list.getLast() + " avg=" + average + " range=" + range);

            list.clear();
        } else if(ms > 0 && ms < 400) {
            list.add(ms);
        }
        lastTimeStamp = timeStamp;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
