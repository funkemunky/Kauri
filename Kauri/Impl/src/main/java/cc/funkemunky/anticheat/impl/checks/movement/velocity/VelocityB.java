package cc.funkemunky.anticheat.impl.checks.movement.velocity;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MathHelper;
import cc.funkemunky.api.utils.MathUtils;
import lombok.val;
import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

//TODO Recode this shit too
@Packets(packets = {
        Packet.Client.POSITION,
        Packet.Client.POSITION_LOOK,
        Packet.Client.LOOK,
        Packet.Client.FLYING,
        Packet.Server.ENTITY_VELOCITY})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "Velocity (Type B)", description = "Checks for horizontal velocity modifications.",
        type = CheckType.VELOCITY, maxVL = 20)
public class VelocityB extends Check {

    private float vl, velocityX, velocityZ, velocityY;
    private long velocityTimestamp, lagTime;

    private static List<float[]> motions = Arrays.asList(
            new float[] {0, 0},
            new float[] {0, 0.98f},
            new float[] {0, -.98f},
            new float[] {.98f, 0},
            new float[] {.98f, 0.98f},
            new float[] {.98f, -.98f},
            new float[] {-.98f, 0},
            new float[] {-.98f, .98f},
            new float[] {-.98f, -.98f});

    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(packetType.equals(Packet.Server.ENTITY_VELOCITY)) {
            WrappedOutVelocityPacket velocity = new WrappedOutVelocityPacket(packet, getData().getPlayer());

            if(velocity.getId() == getData().getPlayer().getEntityId()) {
                velocityX = (float) velocity.getX();
                velocityY = (float) velocity.getY();
                velocityZ = (float) velocity.getZ();
                velocityTimestamp = timeStamp;
                debug("Sent velocity (" + System.currentTimeMillis() + ") ["
                        + velocityX + ", " + velocityY + ", " + velocityZ + "]");
            }
        } else {
            val move = getData().getMovementProcessor();
            val velocity = getData().getVelocityProcessor();

            long delta = timeStamp - velocityTimestamp, ping = getData().getTransPing();
            long deltaTicks = MathUtils.millisToTicks(delta), pingTicks = MathUtils.millisToTicks(ping);

            if(velocityY > 0 && MathUtils.approxEquals(0.01, velocityY, move.getDeltaY())) {
                if(getData().getBoundingBox().shrink(0, 0.1f, 0)
                        .grow(1,0,1)
                        .getCollidingBlockBoxes(getData().getPlayer()).size() == 0) {
                    //debug(velocityX + ", " + velocityZ);
                    float friction = getData().getActionProcessor().isSprinting() ? 0.026f : 0.02f;

                    List<double[]> arrays = new ArrayList<>();

                    motions.forEach(array -> arrays.add(
                            moveFlying(array[0], array[1], MathUtils.yawTo180F(move.getTo().getYaw()), 0.026f)));
                    motions.forEach(array -> arrays.add(
                            moveFlying(array[0], array[1], MathUtils.yawTo180F(move.getTo().getYaw()), 0.02f)));

                    double[] min = arrays.stream().min(Comparator.comparing(array -> {
                        double velocityXZ = Math.hypot(array[0], array[1]);

                        return MathUtils.getDelta(velocityXZ, move.getDeltaXZ());
                    })).get();

                    double velocityXZ =
                            Math.hypot(
                                    min[0] * ((getData().getLastAttack().hasNotPassed(0) ? 0.6f : 1)  *
                                            (move.getFrom().getY() % 1 != 0 || move.getLastDeltaXZ() == 0
                                                    ? 1
                                                    : (1 - (move.getBaseSpeed() / 2))))
                                    , min[1] * (getData().getLastAttack().hasNotPassed(0) ? 0.6f : 1)) *
                                    (move.getFrom().getY() % 1 != 0 || move.getLastDeltaXZ() == 0
                                            ? 1
                                            : (1 - (move.getBaseSpeed() / 2)));

                    double pct = move.getDeltaXZ() / velocityXZ * 100;

                    if(pct < 99.5) {
                        if((vl+= move.getFrom().getY() % 1 != 0 ? 2 : 1) > 7) {
                            flag("vl=" + vl + " pct=" + pct + "%", true, true, AlertTier.HIGH);
                        }
                        debug(Color.Green + "Flag: " + pct + "%: " + vl);
                    } else vl-= vl > 0 ? 1 : 0;

                    debug("sprint: " + velocityXZ + ", " + move.getDeltaXZ());

                    debug("(" + velocityX + ", " + velocityZ + "), (" + move.getDeltaX() + ", "
                            + move.getDeltaZ() + ")");
                }
                velocityY = velocityX = velocityZ = 0;
            }
        }
    }

    private double[] moveFlying(float strafe, float forward, float yaw, float friction) {
        float f = strafe * strafe + forward * forward;
        double velocityX = this.velocityX, velocityZ = this.velocityZ;

        if (f >= 1.0E-4F) {
            f = MathHelper.sqrt_float(f);

            if (f < 1.0F) {
                f = 1.0F;
            }

            f = friction / f;
            strafe = strafe * f;
            forward = forward * f;
            float f1 = MathHelper.sin(yaw * (float) Math.PI / 180.0F);
            float f2 = MathHelper.cos(yaw * (float) Math.PI / 180.0F);
            velocityX +=  (strafe * f2 - forward * f1);
            velocityZ += (forward * f2 + strafe * f1);
        }
        return new double [] {velocityX, velocityZ};
    }

    @Override
    public void onBukkitEvent(Event event) {

    }

    private long millisToTicks(long millis) {
        return (long) Math.ceil(millis / 50D);
    }
}