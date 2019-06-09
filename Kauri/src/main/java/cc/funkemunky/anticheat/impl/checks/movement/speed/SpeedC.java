package cc.funkemunky.anticheat.impl.checks.movement.speed;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.BukkitEvents;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.api.utils.BlockUtils;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.ReflectionsUtil;
import lombok.val;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;

@CheckInfo(name = "Speed (Type C)", description = "Ensures that the acceleration of a player is normal.", type = CheckType.SPEED)
@Init
@BukkitEvents(events = {PlayerMoveEvent.class})
public class SpeedC extends Check {

    /*
    What could cause false positives: water, ladders, slimes, initial grounding or jumping, ice.
     */
    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {

    }

    private int groundTicks, airTicks, vl;
    private double lastDxz;

    @Override
    public void onBukkitEvent(Event event) {
        PlayerMoveEvent e = (PlayerMoveEvent) event;

        val dxz = MathUtils.hypot(e.getTo().getX() - e.getFrom().getX(), e.getTo().getZ() - e.getFrom().getZ());
        val onGround = e.getPlayer().isOnGround();

        if(onGround) {
            groundTicks++;
            airTicks = 0;
        } else {
            airTicks++;
            groundTicks = 0;
        }

        val underBlock = BlockUtils.getBlock(e.getPlayer().getLocation().clone().subtract(0, 0.25,0));
        val decel = onGround ? ReflectionsUtil.getFriction(underBlock) : (getData().getActionProcessor().isSprinting() ? 0.026f : 0.02f);
        val difference = MathUtils.getDelta(lastDxz, dxz);

        if(airTicks > 3 && !getData().isLagging() && getData().getLastLag().hasPassed(5) && MathUtils.getDelta(decel, difference) > 0.03 && !MiscUtils.cancelForFlight(getData(), 8, false)) {
            if(vl++ > 4) {
                flag(difference + ">-" + decel, true, true, AlertTier.HIGH);
            } else flag(difference + ">-" + decel, true, false, AlertTier.POSSIBLE);
        } else vl-= vl > 0 ? 1 : 0;


        debug("decel=" + decel + " difference=" + difference + " vl=" + vl);
        lastDxz = dxz;
    }
}
