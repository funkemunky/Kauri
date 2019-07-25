package cc.funkemunky.anticheat.impl.checks.combat.aimassist;

import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Interval;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import lombok.val;
import org.bukkit.event.Event;

@Init
@CheckInfo(name = "Aim (Type J)", type = CheckType.AIM)
@Packets(packets = {Packet.Client.LOOK, Packet.Client.POSITION_LOOK})
public class AimJ extends Check {

    private Interval<Float> interval = new Interval<>(0, 20);
    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        if(interval.size() >= 19) {
            debug("distinct=" + interval.distinct() + " avg=" + interval.average() + " range=" + (interval.max() - interval.min()));
        }

        debug("size=" + interval.size());

        interval.add(move.getYawDelta());
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
