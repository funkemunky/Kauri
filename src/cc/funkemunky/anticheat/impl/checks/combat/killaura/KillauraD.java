package cc.funkemunky.anticheat.impl.checks.combat.killaura;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import lombok.val;
import org.bukkit.event.Event;

import java.util.Deque;
import java.util.LinkedList;

@Packets(packets = {
        Packet.Client.POSITION_LOOK,
        Packet.Client.LOOK,
        Packet.Client.LEGACY_POSITION_LOOK,
        Packet.Client.LEGACY_LOOK})
public class KillauraD extends Check {

    private float lastYaw, lastPitch;
    private Deque<Float> yawDeque = new LinkedList<>(),
            pitchDeque = new LinkedList<>();
    private int vl;

    public KillauraD(String name, CancelType cancelType, int maxVL) {
        super(name, cancelType, maxVL);
    }

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val yaw = this.getData().getPlayer().getLocation().getYaw();
        val pitch = this.getData().getPlayer().getLocation().getPitch();

        val yawChange = Math.abs(yaw - lastYaw);
        val pitchChange = Math.abs(pitch - lastPitch);

        yawDeque.add(yawChange);
        yawDeque.add(pitchChange);

        if (yawDeque.size() == 20 && pitchDeque.size() == 20) {
            val yawDistinct = yawDeque.stream().distinct().count();
            val pitchDistinct = pitchDeque.stream().distinct().count();

            val yawDups = yawDeque.size() - yawDistinct;
            val pitchDups = pitchDeque.size() - pitchDistinct;

            if (yawDups == 0 && pitchDups == 0) {
                if (++vl > 3) {
                    flag("P|Y: 0", true, false);
                }
            } else {
                vl = 0;
            }

            yawDeque.clear();
            pitchDeque.clear();
        }

        this.lastYaw = yaw;
        this.lastPitch = pitch;

        debug(vl + ": " + yawChange);
    }

    @Override
    public void onBukkitEvent(Event event) {
    }
}
