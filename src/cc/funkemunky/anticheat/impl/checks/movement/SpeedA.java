package cc.funkemunky.anticheat.impl.checks.movement;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Verbose;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.PlayerUtils;
import lombok.val;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffectType;

@Packets(packets = {Packet.Client.POSITION_LOOK,
        Packet.Client.POSITION,
        Packet.Client.LEGACY_POSITION_LOOK,
        Packet.Client.LEGACY_POSITION})
public class SpeedA extends Check {

    private Verbose verbose = new Verbose();
    private long lastTimeStamp;

    public SpeedA(String name, CancelType cancelType, int maxVL) {
        super(name, cancelType, maxVL);

    }

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        //The client will always send a position packet when teleported or dictated to move by the server, so we need to account for that to prevent false-positives.
        if (getData().getLastServerPos().hasNotPassed(1) || getData().isGeneralCancel()) {
            return;
        }
        val to = getData().getMovementProcessor().getTo();
        val from = getData().getMovementProcessor().getFrom();

        /* We we do just a basic calculation of the maximum allowed movement of a player */
        float motionXZ = (float) Math.hypot(to.getX() - from.getX(), to.getZ() - from.getZ());

        /* We we do just a basic calculation of the maximum allowed movement of a player */
        double baseSpeed;


        if (getData().getMovementProcessor().getAirTicks() > 0) {
            baseSpeed = 0.369 * Math.pow(0.988, Math.min(16, getData().getMovementProcessor().getAirTicks()));
        } else {
            baseSpeed = 0.341 - (0.0052 * Math.min(9, getData().getMovementProcessor().getGroundTicks()));
        }

        baseSpeed += PlayerUtils.getPotionEffectLevel(getData().getPlayer(), PotionEffectType.SPEED) * (getData().getMovementProcessor().isServerOnGround() ? 0.058f : 0.044f);
        baseSpeed *= getData().getMovementProcessor().getHalfBlockTicks() > 0 ? 2.5 : 1;
        baseSpeed *= getData().getMovementProcessor().getBlockAboveTicks() > 0 ? 3.4 : 1;
        baseSpeed *= getData().getMovementProcessor().getIceTicks() > 0 && getData().getMovementProcessor().getGroundTicks() < 6 ? 2.5f : 1.0;
        baseSpeed += getData().getLastBlockPlace().hasNotPassed(15) ? 0.1 : 0;
        baseSpeed += (getData().getPlayer().getWalkSpeed() - 0.2) * 1.8f;
        baseSpeed += getData().isOnSlimeBefore() ? 0.1 : 0;

        if (timeStamp - lastTimeStamp > 1) {
            if (motionXZ > baseSpeed && !getData().getVelocityProcessor().getLastVelocity().hasNotPassed(40)) {
                if (verbose.flag(getData().isLagging() ? 45 : 35, 1000L, motionXZ - baseSpeed > 0.5f ? 5 : 2)) {
                    flag(MathUtils.round(motionXZ, 4) + ">-" + MathUtils.round(baseSpeed, 4), false, true);
                }
            } else {
                verbose.deduct();
            }

        }
        lastTimeStamp = timeStamp;

        debug(verbose.getVerbose() + ": " + motionXZ + ", " + baseSpeed + ", " + getData().getMovementProcessor().getDistanceToGround() + ", " + getData().getMovementProcessor().getAirTicks() + ", " + getData().getMovementProcessor().getGroundTicks() + ", " + getData().getPlayer().getWalkSpeed());
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
