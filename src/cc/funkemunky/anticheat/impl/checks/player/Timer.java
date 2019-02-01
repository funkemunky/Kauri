package cc.funkemunky.anticheat.impl.checks.player;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckType;
import org.bukkit.event.Event;

public class Timer extends Check {

    public Timer(String name, CheckType type, CancelType cancelType, int maxVL) {
        super(name, type, cancelType, maxVL);
    }

    @Override
    public Object onPacket(Object packet, String packetType, long timeStamp) {
        return packet;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
