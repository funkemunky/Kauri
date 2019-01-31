package cc.funkemunky.anticheat.impl.checks.combat.fastbow;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.BukkitEvents;
import cc.funkemunky.anticheat.api.utils.TickTimer;
import cc.funkemunky.anticheat.api.utils.Verbose;
import cc.funkemunky.api.utils.MathUtils;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.bukkit.event.entity.ProjectileLaunchEvent;

@BukkitEvents(events = {ProjectileLaunchEvent.class})
public class Fastbow extends Check {
    private TickTimer lastShoot = new TickTimer(10);
    private Verbose verbose = new Verbose();
    public Fastbow(String name, CheckType type, CancelType cancelType, int maxVL) {
        super(name, type, cancelType, maxVL);

    }

    @Override
    public Object onPacket(Object packet, String packetType, long timeStamp) {
        return packet;
    }

    @Override
    public void onBukkitEvent(Event event) {
        ProjectileLaunchEvent e = (ProjectileLaunchEvent) event;

        if (!e.getEntity().getType().equals(EntityType.ARROW)) return;

        Arrow arrow = (Arrow) e.getEntity();

        if (arrow.getVelocity().length() > .14 && lastShoot.hasNotPassed(6)) {
            if (verbose.flagB(5, 1)) {
                flag("t: " + lastShoot + "; v: " + MathUtils.round(arrow.getVelocity().length(), 5), false, true);
            }
        } else {
            verbose.deduct();
        }

        debug(verbose.getVerbose() + ": " + arrow.getVelocity().length() + ", " + lastShoot.getPassed());
        lastShoot.reset();
    }
}