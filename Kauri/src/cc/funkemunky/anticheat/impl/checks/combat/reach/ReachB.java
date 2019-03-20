package cc.funkemunky.anticheat.impl.checks.combat.reach;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.DynamicRollingAverage;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import lombok.val;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

@Packets(packets = {Packet.Client.USE_ENTITY})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "Reach (Type B)", description = "A simple and light, but extremely effective maximum reach calculation. However, slightly experimental.", developer = true, maxVL = 60, executable = false)
public class ReachB extends Check {
    @Setting(name = "threshold.vl.max")
    static double maxVL = 8.0;
    @Setting(name = "threshold.vl.deduct")
    static double deductVL = 0.5;
    @Setting(name = "threshold.reach")
    static double maxReach = 3.4;

    private DynamicRollingAverage reachAvg = new DynamicRollingAverage(5);
    private double vl;

    public ReachB() {

    }

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        WrappedInUseEntityPacket useEvent = new WrappedInUseEntityPacket(packet, getData().getPlayer());

        if (getData().isGeneralCancel()) return;
        if (useEvent.getAction().equals(WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK) && useEvent.getEntity() instanceof Player) {
            val player = getData().getPlayer();
            val entity = (Player) useEvent.getEntity();

            val entityData = Kauri.getInstance().getDataManager().getPlayerData(entity.getUniqueId());

            if (entityData == null) return;

            double var9 = entity.getLocation().getX() - player.getLocation().getX();
            double var7;

            double mx = Math.cos(Math.toRadians(entity.getLocation().getYaw() + 90.0F));
            double mz = Math.sin(Math.toRadians(entity.getLocation().getYaw() + 90.0F));

            double x = (1 * 1 * mx + 0 * 1 * mz);
            double z = (1 * 1 * mz - 0 * 1 * mx);

            if (!entity.isSprinting()) {
                val dXX = Math.abs((entityData.getMovementProcessor().getFrom().getX() - x) - (player.getLocation().getX()));
                val dZZ = Math.abs((entityData.getMovementProcessor().getFrom().getZ() - z) - (player.getLocation().getZ()));

                val dX = Math.abs(entityData.getMovementProcessor().getFrom().getX() - player.getLocation().getX());
                val dZ = Math.abs(entityData.getMovementProcessor().getFrom().getZ() - player.getLocation().getZ());

                if (dXX + dZZ < dX + dZ) {
                    return;
                }
            }

            // calculate real-time velocity
            for (var7 = entity.getLocation().getZ() - player.getLocation().getZ(); var9 * var9 + var7 * var7 < 1.0E-4D; var7 = (Math.random() - Math.random()) * 0.01D) {
                var9 = (Math.random() - Math.random()) * 0.01D;
            }

            double motionX = getData().getPlayer().getVelocity().getX() / 2.0;
            double motionY = getData().getPlayer().getVelocity().getY() / 2.0;
            double motionZ = getData().getPlayer().getVelocity().getZ() / 2.0;

            float var71 = (float) Math.sqrt(var9 * var9 + var7 * var7);
            float var8 = 0.4F;

            motionX -= var7 / (double) var71 * (double) var8;
            motionY += (double) var8;
            motionZ -= var9 / var7 * (double) var8;

            if (motionY > 0.4000000059604645D) {
                motionY = 0.4000000059604645D;
            }

            val distance = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);

            val dX = entity.getLocation().getX() - player.getLocation().getX();
            val dZ = entity.getLocation().getZ() - player.getLocation().getZ();

            val reachDistance = Math.sqrt(dX * dX + dZ * dZ - distance);

            if (!entity.isOnGround()) {
                return;
            }

            reachAvg.add(reachDistance);

            val reachAverage = reachAvg.getAverage();

            if (reachAverage > maxReach) {
                if (vl++ > maxVL) {
                    flag(reachAverage + ">-" + maxReach, true, true);
                }
            } else {
                vl -= vl > 0 ? deductVL : 0;
            }

            debug(reachAverage + ", " + vl);

            if (reachAvg.isReachedSize()) {
                reachAvg.clearValues();
            }
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}