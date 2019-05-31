package cc.funkemunky.anticheat.impl.checks.combat.killaura;

import cc.funkemunky.anticheat.api.checks.*;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.TickTimer;
import lombok.val;
import org.bukkit.event.Event;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

@Packets(packets = {
        Packet.Client.POSITION_LOOK,
        Packet.Client.LOOK,
        Packet.Client.LEGACY_POSITION_LOOK,
        Packet.Client.LEGACY_LOOK})
@CheckInfo(name = "Killaura (Type E)", description = "A heuristic which factors in the rotations to look for any patterns.", type = CheckType.KILLAURA, cancelType = CancelType.COMBAT)
@Init
public class KillauraD extends Check {

    private final Deque<Float> pitchDeque = new LinkedList<>(), yawDeque = new LinkedList<>();
    private final AtomicInteger level = new AtomicInteger();
    private int vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val from = getData().getMovementProcessor().getFrom();
        val to = getData().getMovementProcessor().getTo();

        val yawChange = Math.abs(from.getYaw() - to.getYaw());
        val pitchChange = Math.abs(from.getPitch() - to.getPitch());

        yawDeque.add(yawChange);
        pitchDeque.add(pitchChange);


        if (yawDeque.size() == 5 && pitchDeque.size() == 5) {
            yawDeque.stream().filter(yaw -> yaw != 0.f).forEach(yaw -> {
                if (yaw > 10.f) {
                    level.getAndIncrement();
                }
            });

            pitchDeque.stream().filter(pitch -> pitch != 0.f).forEach(pitch -> {
                if (pitch > 7.f) {
                    level.getAndIncrement();
                }
            });

            val pitchAverage = pitchDeque.stream().mapToDouble(Float::floatValue).average().orElse(0.0);
            val yawAverage = yawDeque.stream().mapToDouble(Float::floatValue).average().orElse(0.0);

            if ((pitchAverage >= 10.0F && yawAverage >= 5.6) && level.get() >= 3 && level.get() < 6) {
                if(vl++ > 2) {
                    flag(pitchAverage + " -> " + yawAverage + " -> 0.0", false, false, AlertTier.HIGH);
                } else flag(pitchAverage + " -> " + yawAverage + " -> 0.0", false, false, AlertTier.POSSIBLE);
            } else vl-= vl > 0 ? 1 : 0;

            yawDeque.clear();
            pitchDeque.clear();
            level.set(0);
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
