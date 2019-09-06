package cc.funkemunky.anticheat.impl.checks.combat.fastbow;

import cc.funkemunky.anticheat.api.checks.*;
import cc.funkemunky.anticheat.api.utils.BukkitEvents;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.bukkit.event.entity.ProjectileLaunchEvent;

@BukkitEvents(events = {ProjectileLaunchEvent.class})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "Fastbow", description = "Makes sure the rate of fire is legitimate.",
        type = CheckType.COMBAT,
        cancelType = CancelType.INTERACT,
        maxVL = 20,
        executable = true)
public class Fastbow extends Check {
    private long lastShoot;
    private int vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
    }

    @Override
    public void onBukkitEvent(Event event) {
        ProjectileLaunchEvent e = (ProjectileLaunchEvent) event;

        if (!e.getEntity().getType().equals(EntityType.ARROW)) return;

        Arrow arrow = (Arrow) e.getEntity();

        long elapsed = System.currentTimeMillis() - lastShoot,
                threshold = Math.round(arrow.getVelocity().length() * 250L);

        if(elapsed < threshold) {
            if(vl++ > 3) {
                flag("vl=" + vl + " threshold=" + threshold + " elapsed=" + elapsed,
                        true, true, AlertTier.HIGH);
            }
        } else vl-= vl > 0 ? 1 : 0;

        debug(vl+ ": " + arrow.getVelocity().length() + ", " + elapsed);
        lastShoot = System.currentTimeMillis();
    }
}