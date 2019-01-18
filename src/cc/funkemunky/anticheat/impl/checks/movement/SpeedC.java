package cc.funkemunky.anticheat.impl.checks.movement;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.*;
import lombok.val;
import lombok.var;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffectType;

@Packets(packets = {Packet.Client.POSITION_LOOK,
        Packet.Client.POSITION,
        Packet.Client.LEGACY_POSITION_LOOK,
        Packet.Client.LEGACY_POSITION})
public class SpeedC extends Check {
    private long lastTimeStamp;
    private int threshold;
    private double lastSpeed;

    public SpeedC(String name, CancelType cancelType, int maxVL) {
        super(name, cancelType, maxVL);
    }

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        WrappedInFlyingPacket flying = new WrappedInFlyingPacket(packet, getData().getPlayer());
        if (getData().getLastServerPos().hasNotPassed(2) || getData().isGeneralCancel()) return;

        if (timeStamp - lastTimeStamp > 1) {

            val to = getData().getMovementProcessor().getTo();
            val from = getData().getMovementProcessor().getFrom();

            val dx = to.getX() - from.getX();
            val dy = to.getY() - from.getY();
            val dz = to.getZ() - from.getZ();

            var f5 = 0.91f;

            val onGround = flying.isGround();

            //noinspection
            @SuppressWarnings("UnusedAssignment")
            var moveSpeed = 0.0f;

            if (onGround) {
                f5 = ReflectionsUtil.getFriction(BlockUtils.getBlock(new Location(getData().getPlayer().getWorld(), MathUtils.floor(to.getX()),
                        MathUtils.floor(getData().getBoundingBox().minY) - 1,
                        MathUtils.floor(to.getZ())))) * 0.91F;
            }

            val f6 = 0.16277136F / (f5 * f5 * f5);

            if (onGround) {
                moveSpeed = Atlas.getInstance().getBlockBoxManager().getBlockBox().getMovementFactor(getData().getPlayer()) * f6;

                //fixes a speed bug
                if (getData().getPlayer().isSprinting() && moveSpeed < 0.129) {
                    moveSpeed *= 1.3;
                }

                //fixes momentum when you land
                if (dy > 0.0001) {
                    moveSpeed += 0.2;
                }
            } else {
                moveSpeed = (getData().getPlayer().isSprinting() ? 0.026f : 0.02f) + 0.00001f;

                if (dy < -0.08 && getData().getPlayer().getFallDistance() == 0.0) {
                    moveSpeed *= Math.abs(dy) * 1.3;
                }
            }

            val previousSpeed = this.lastSpeed;
            val speed = Math.sqrt(dx * dx + dz * dz);

            val speedChange = speed - previousSpeed;

            moveSpeed += Math.sqrt(this.getData().getVelocityProcessor().getMaxHorizontal());

            if (speed > 0.24 && speedChange - moveSpeed > 0.001) {
                if ((threshold += 10) > 15) {
                    flag(speedChange + ">-" + moveSpeed, true, true);
                }
            } else {
                threshold = Math.max(threshold - 1, 0);
            }

            this.lastSpeed = speed * f5;
        }

        lastTimeStamp = timeStamp;

        debug(getData().getMovementProcessor().getDeltaXZ() + ", " + getData().getMovementProcessor().getDeltaY() + ", " + getData().getMovementProcessor().getDistanceToGround() + ", " + getData().getMovementProcessor().getHalfBlockTicks() + ", " + getData().getMovementProcessor().getBlockAboveTicks());
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
