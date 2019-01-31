package cc.funkemunky.anticheat.impl.checks.player;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.BukkitEvents;
import cc.funkemunky.anticheat.api.utils.Verbose;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityRegainHealthEvent;

@BukkitEvents(events = {EntityRegainHealthEvent.class})
public class BadPacketsE extends Check {

    private Verbose verbose = new Verbose();

    public BadPacketsE(String name, CheckType type, CancelType cancelType, int maxVL) {
        super(name, type, cancelType, maxVL);
    }

    @Override
    public Object onPacket(Object packet, String packetType, long timeStamp) {
        return packet;
    }

    @Override
    public void onBukkitEvent(Event event) {
        EntityRegainHealthEvent e = (EntityRegainHealthEvent) event;

        if (!e.getRegainReason().equals(EntityRegainHealthEvent.RegainReason.SATIATED)) return;

        if (verbose.flag(2, ProtocolVersion.getGameVersion().isBelow(ProtocolVersion.V1_9) ? 750L : 450L)) {
            flag(verbose.getLastFlag().getPassed() + "<-15", false, true);
        }
    }
}