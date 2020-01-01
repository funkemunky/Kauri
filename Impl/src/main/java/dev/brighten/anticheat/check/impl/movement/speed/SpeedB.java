package dev.brighten.anticheat.check.impl.movement.speed;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.PlayerUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import lombok.val;
import lombok.var;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

@CheckInfo(name = "Speed (B)", description = "A simple averaging speed check. By Elevated.",
        checkType = CheckType.SPEED, punishVL = 8)
public class SpeedB extends Check {
    private double lastX, lastY, lastZ;
    private int verbose;

    private final Deque<Double> offsetDeque = new LinkedList<>();

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(!packet.isPos()) return;

        val player = data.getPlayer();

        val from = data.playerInfo.from;
        val to = data.playerInfo.to;

        val fromVec = from.toVector();
        val toVec = to.toVector();

        val motionX = data.playerInfo.deltaX;
        val motionY = data.playerInfo.deltaY;
        val motionZ = data.playerInfo.deltaZ;

        val lastMotLoc = new Vector(lastX, lastY, lastZ);
        val currMotLoc = new Vector(motionX, motionY, motionZ);

        val locOffset = MathUtils.offset(fromVec, toVec);
        val motOffset = MathUtils.offset(lastMotLoc, currMotLoc);

        val offsetChange = Math.abs(locOffset - motOffset);

        val streak = new AtomicInteger();

        if (data.playerInfo.canFly
                || data.playerInfo.lastVelocity.hasNotPassed(8)
                || player.getVehicle() != null
                || data.playerInfo.riptiding
                || data.playerInfo.gliding) {
            return;
        }

        offsetDeque.add(offsetChange);

        if (offsetDeque.size() == 5) {
            val account = account();
            val maxOffset = 0.335 + account;

            for (double offset : offsetDeque) {
                if (offset > maxOffset) {
                    streak.incrementAndGet();
                }
            }

            var maxStreak = 2;

            if (player.hasPotionEffect(PotionEffectType.SPEED)) {
                maxStreak++;
            }

            float pct = (streak.get() * 100F) / maxStreak;
            if (streak.get() >= maxStreak) {
                if (verbose++ > 2) {
                    vl++;
                    flag( pct + "%");
                }
            } else if (streak.get() == 0) {
                verbose = 0;
            }

            debug(verbose + ": " + streak.get() + ", " + maxStreak + "," + pct + ","
                    + MathUtils.hypot(data.playerInfo.mvx, data.playerInfo.mvz) + ", " + account);

            offsetDeque.clear();
        }

        lastX = motionX;
        lastY = motionY;
        lastZ = motionZ;
    }

    private float account() {
        float total = 0;

        total += PlayerUtils.getPotionEffectLevel(data.getPlayer(), PotionEffectType.SPEED)
                * (data.playerInfo.serverGround ? 0.057f : 0.044f);
        total += data.playerInfo.iceTicks.value() > 0
                && (data.playerInfo.deltaY > 0.001 || data.playerInfo.groundTicks < 6) ? 0.23 : 0;
        total += (data.getPlayer().getWalkSpeed() - 0.2) * 1.65;
        total += (data.playerInfo.lastBlockPlace.hasNotPassed(7)) ? 0.1 : 0;
        total += data.playerInfo.wasOnSlime ? 0.1 : 0;
        total += data.playerInfo.blocksAboveTicks.value() > 0 ? data.playerInfo.iceTicks.value() > 0 ? 0.4 : 0.2 : 0;
        total += data.playerInfo.halfBlockTicks.value() > 0 ? 0.12 : 0;
        total += Math.max(0, MathUtils.hypot(data.playerInfo.mvx, data.playerInfo.mvz) - total);
        return total;
    }
}
