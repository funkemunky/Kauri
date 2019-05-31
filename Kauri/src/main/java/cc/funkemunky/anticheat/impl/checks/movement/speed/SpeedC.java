package cc.funkemunky.anticheat.impl.checks.movement.speed;

import cc.funkemunky.anticheat.api.checks.*;
import cc.funkemunky.anticheat.api.utils.BukkitEvents;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.api.Atlas;
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

        if(MiscUtils.cancelForFlight(getData(), 4, false)) return;

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
        val decel = onGround ? ReflectionsUtil.getFriction(underBlock) : Atlas.getInstance().getBlockBoxManager().getBlockBox().getMovementFactor(e.getPlayer());
        val difference = lastDxz - dxz;

        if(groundTicks > 1 && airTicks > 1 && MathUtils.getDelta(decel, difference) > 1E-6) {
            if(vl++ > 4) {
                flag(difference + ">-" + decel, true, true, AlertTier.HIGH);
            } else flag(difference + ">-" + decel, true, false, AlertTier.LOW);
        } else vl-= vl > 0 ? 1 : 0;


        debug("decel=" + decel + " dxz=" + dxz);
        lastDxz = dxz;
    }
}
