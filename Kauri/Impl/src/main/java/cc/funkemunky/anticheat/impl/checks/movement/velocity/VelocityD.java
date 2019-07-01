package cc.funkemunky.anticheat.impl.checks.movement.velocity;

import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import org.bukkit.event.Event;

@CheckInfo(name = "Velocity (Type D)", description = "A prediction velocity check.")
public class VelocityD extends Check {

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {

    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}