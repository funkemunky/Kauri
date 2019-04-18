package cc.funkemunky.anticheat.impl.checks.combat.fastbow;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.BukkitEvents;
import cc.funkemunky.anticheat.api.utils.Verbose;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.TickTimer;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.bukkit.event.entity.ProjectileLaunchEvent;

@BukkitEvents(events = {ProjectileLaunchEvent.class})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "Fastbow", description = "Makes sure the rate of fire is legitimate.", type = CheckType.COMBAT, cancelType = CancelType.INTERACT, maxVL = 20)
public class Fastbow extends Check {

    private TickTimer lastShoot = new TickTimer(10);
    private Verbose verbose = new Verbose();

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {

    }

    @Override
    public void onBukkitEvent(Event event) {
        ProjectileLaunchEvent e = (ProjectileLaunchEvent) event;

        if (!e.getEntity().getType().equals(EntityType.ARROW)) return;

        Arrow arrow = (Arrow) e.getEntity();

        if (arrow.getVelocity().length() > .14 && lastShoot.hasNotPassed(6)) {
            if (verbose.flagB(5, 1)) {
                flag("t: " + lastShoot.getPassed() + "; v: " + MathUtils.round(arrow.getVelocity().length(), 5), false, true);
            }
        } else {
            verbose.deduct();
        }

        debug(verbose.getVerbose() + ": " + arrow.getVelocity().length() + ", " + lastShoot.getPassed());
        lastShoot.reset();
    }
}