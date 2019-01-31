package cc.funkemunky.anticheat.impl.checks.movement;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.PlayerUtils;
import lombok.val;
import lombok.var;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffectType;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

@Packets(packets = {
        Packet.Client.POSITION_LOOK,
        Packet.Client.POSITION,
        Packet.Client.LEGACY_POSITION_LOOK,
        Packet.Client.LEGACY_POSITION})
public class SpeedC extends Check {
    private double lastX, lastY, lastZ;
    private int vl;

    private final Deque<Double> offsetDeque = new LinkedList<>();

    public SpeedC(String name, CheckType type, CancelType cancelType, int maxVL) {
        super(name, type, cancelType, maxVL);
    }

    @Override
    public Object onPacket(Object packet, String packetType, long timeStamp) {
        val player = getData().getPlayer();

        val from = getData().getMovementProcessor().getFrom();
        val to = getData().getMovementProcessor().getTo();

        val fromVec = from.toVector();
        val toVec = to.toVector();

        val motionX = to.getX() - from.getX();
        val motionY = to.getY() - from.getY();
        val motionZ = to.getZ() - from.getZ();

        val lastMotLoc = new Location(player.getWorld(), lastX, lastY, lastZ).toVector();
        val currMotLoc = new Location(player.getWorld(), motionX, motionY, motionZ).toVector();

        val locOffset = MathUtils.offset(fromVec, toVec);
        val motOffset = MathUtils.offset(lastMotLoc, currMotLoc);

        val offsetChange = Math.abs(locOffset - motOffset);

        val streak = new AtomicInteger();

        if (getData().isGeneralCancel()) {
            return packet;
        }

        offsetDeque.add(offsetChange);

        if (offsetDeque.size() == 5) {
            val maxOffset = 0.33 + account();

            offsetDeque.forEach(offset -> {
                if (offset > maxOffset) {
                    streak.incrementAndGet();
                }
            });

            var maxStreak = 2;

            if (player.hasPotionEffect(PotionEffectType.SPEED)) {
                maxStreak++;
            }

            if (streak.get() >= maxStreak) {
                if (++vl > 2) {
                    flag( "" + (double) (streak.get() * 100) / maxStreak + "%", true, true);
                }
            } else {
                vl = 0;
            }

            debug(streak.get() + ", " + maxStreak);

            offsetDeque.clear();
        }

        lastX = motionX;
        lastY = motionY;
        lastZ = motionZ;
        return packet;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }

    private float account() {
        float total = 0;

        total += PlayerUtils.getPotionEffectLevel(getData().getPlayer(), PotionEffectType.SPEED) * (getData().getMovementProcessor().isServerOnGround() ? 0.057f : 0.044f);
        total += getData().getMovementProcessor().getIceTicks() > 0 && (getData().getMovementProcessor().getAirTicks() > 0 || getData().getMovementProcessor().getGroundTicks() < 5) ? 0.14 : 0;
        total += (getData().getPlayer().getWalkSpeed() - 0.2) * 1.65;
        total += getData().getMovementProcessor().isOnSlimeBefore() ? 0.05 : 0;
        total += getData().getMovementProcessor().getBlockAboveTicks() > 0 ? getData().getMovementProcessor().isOnIce() ? 0.4 : 0.2  : 0;
        total += getData().getMovementProcessor().getHalfBlockTicks() > 0 ? 0.12 : 0;
        return total;
    }
}