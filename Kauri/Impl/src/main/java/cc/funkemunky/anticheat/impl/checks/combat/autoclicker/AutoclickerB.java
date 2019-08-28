package cc.funkemunky.anticheat.impl.checks.combat.autoclicker;

import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.api.utils.Init;
import org.bukkit.event.Event;

@Init
@CheckInfo(name = "Autocli")
public class AutoclickerB extends Check {
    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {

    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
