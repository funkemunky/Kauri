package cc.funkemunky.anticheat.impl.checks.player.badpackets;

import cc.funkemunky.anticheat.api.checks.*;
import cc.funkemunky.anticheat.api.utils.BukkitEvents;
import cc.funkemunky.anticheat.api.utils.Verbose;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityRegainHealthEvent;

@BukkitEvents(events = {EntityRegainHealthEvent.class})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "BadPackets (Type E)", description = "Checks the rate of healing.", type = CheckType.BADPACKETS, cancelType = CancelType.HEALTH, maxVL = 20, executable = true)
public class BadPacketsE extends Check {

    private Verbose verbose = new Verbose();

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
    }

    @Override
    public void onBukkitEvent(Event event) {
        EntityRegainHealthEvent e = (EntityRegainHealthEvent) event;

        if (!e.getRegainReason().equals(EntityRegainHealthEvent.RegainReason.SATIATED)) return;

        if (verbose.flag(2, 450)) {
            flag(verbose.getLastFlag().getPassed() + "<-15", false, true, verbose.getVerbose() > 5 ? AlertTier.CERTAIN : AlertTier.HIGH);
        }
    }
}