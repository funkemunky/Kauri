package cc.funkemunky.anticheat.impl.checks.movement;


import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.utils.BukkitEvents;
import cc.funkemunky.api.utils.PlayerUtils;
import lombok.val;
import lombok.var;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffectType;

@BukkitEvents(events = {PlayerMoveEvent.class})
public class Clip extends Check {
    public Clip(String name, CancelType cancelType, int maxVL) {
        super(name, cancelType, maxVL);
    }

    private double lastDeltaY;
    @Override
    public Object onPacket(Object packet, String packetType, long timeStamp) {

        return packet;
    }

    @Override
    public void onBukkitEvent(Event event) {
        PlayerMoveEvent e = (PlayerMoveEvent) event;

        if(e.getPlayer().getAllowFlight() || getData().getVelocityProcessor().getLastVelocity().hasNotPassed(20) || e.getPlayer().getVehicle() != null || PlayerUtils.isGliding(e.getPlayer()) || getData().getMovementProcessor().isRiptiding()) {
            return;
        }

        val motionXZ = Math.hypot(e.getTo().getX() - e.getFrom().getX(), e.getTo().getZ() - e.getFrom().getZ());
        val motionY = e.getTo().getY() - e.getFrom().getY();

        var totalMax = 1 + account();
        var lagMax = 0.7 + account();
        if(motionXZ > totalMax || (motionXZ > lagMax && !getData().isLagging())) {
            flag(motionXZ  + ">-" + lagMax, true, true);
        }

        val yMax = 0.6 + PlayerUtils.getPotionEffectLevel(getData().getPlayer(), PotionEffectType.JUMP) * 0.2f;

        if(Math.abs(lastDeltaY - motionY) > yMax && getData().getMovementProcessor().getDistanceToGround() > 1.5) {
            flag(Math.abs(lastDeltaY - motionY) + ">-" + yMax, true, true);
        }

        lastDeltaY = motionY;
    }

    private float account() {
        float total = 0;

        total += PlayerUtils.getPotionEffectLevel(getData().getPlayer(), PotionEffectType.SPEED) * 0.07f;
        total += getData().getMovementProcessor().getIceTicks() > 0 && (getData().getMovementProcessor().getAirTicks() > 0 || getData().getMovementProcessor().getGroundTicks() < 7) ? 0.23 : 0;
        total += (getData().getPlayer().getWalkSpeed() - 0.2) * 3.0;
        total += (getData().getLastBlockPlace().hasNotPassed(7)) ? 0.3 : 0;
        total += getData().getMovementProcessor().isOnSlimeBefore() ? 0.3 : 0;
        total += getData().getMovementProcessor().getBlockAboveTicks() > 0 ? getData().getMovementProcessor().isOnIce() ? 0.6 : 0.4  : 0;
        total += getData().getMovementProcessor().getHalfBlockTicks() > 0 ? 0.25 : 0;
        return total;
    }
}
