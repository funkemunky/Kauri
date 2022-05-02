package dev.brighten.anticheat.check.impl.movement.velocity;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import cc.funkemunky.api.utils.Tuple;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.anticheat.utils.timer.Timer;
import dev.brighten.anticheat.utils.timer.impl.TickTimer;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;
import lombok.Getter;
import lombok.var;
import org.bukkit.enchantments.Enchantment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@CheckInfo(name = "Velocity (D)", description = "Checks if a player responded to velocity",
        checkType = CheckType.VELOCITY, punishVL = 5, devStage = DevStage.BETA)
@Cancellable(cancelType = CancelType.MOVEMENT)
public class VelocityD extends Check {

    private int buffer;
    private final Timer lastVelocity = new TickTimer();
    private final List<Velocity> velocityY = new CopyOnWriteArrayList<>();

    @Setting(name = "bufferThreshold")
    private static int bufferThreshold = 3;

    @Packet
    public void onVelocity(WrappedOutVelocityPacket packet) {
        if(packet.getId() == data.getPlayer().getEntityId() && packet.getY() > 0.1) {
            data.runKeepaliveAction(ka -> {
                synchronized (velocityY) {
                    velocityY.add(new Velocity(packet.getY()));
                    lastVelocity.reset();
                }
            });
        }
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long now) {
        if(velocityY.size() == 0) return;

        var toRemove = velocityY.stream()
                .filter(t -> Math.abs(data.playerInfo.deltaY - t.getVelocity()) < 0.001)
                .collect(Collectors.toList());

        if(toRemove.size() > 0) {

            if(buffer > 0) buffer--;
            debug("Reset velocity: dy=%.4f b=%s",
                    data.playerInfo.deltaY, buffer);

            //Removing
            for (Velocity v : toRemove) {
                velocityY.remove(v);
            }
            toRemove.clear();
            return;
        }

        //All potential causes of false positives
        if(data.playerInfo.doingBlockUpdate
                || data.playerInfo.webTimer.isNotPassed(3)
                || data.playerInfo.liquidTimer.isNotPassed(3)
                || data.playerInfo.slimeTimer.isNotPassed(2)
                || data.blockInfo.inScaffolding
                || data.blockInfo.inHoney
                || data.playerInfo.blockAboveTimer.isNotPassed(2)) {
            debug("Potential false flag");
            return;
        }

        toRemove = velocityY.stream().filter(t -> now - t.getTimestamp() > 4000L)
                .collect(Collectors.toList());

        if(toRemove.size() > 0) {
            for (Velocity v : toRemove) {
                velocityY.remove(v);
            }

            toRemove.clear();
        } else if(velocityY.size() > 0 && lastVelocity.isPassed(2000L)) {
            if(++buffer > 2) {
                vl++;
                flag("lv=%s s=%s", lastVelocity.getPassed(), velocityY.size());
            }
        }
    }

    @Getter
    public static class Velocity {
        private final double velocity;
        private final long timestamp;

        public Velocity(double velocityY) {
            this.velocity = velocityY;
            this.timestamp = System.currentTimeMillis();
        }
    }

    @CheckInfo(name = "Velocity (B)", description = "A horizontal velocity check.", checkType = CheckType.VELOCITY,
            punishVL = 40, vlToFlag = 15, executable = true)
    @Cancellable
    public static class VelocityB extends Check {

        private double pvX, pvZ;
        private boolean useEntity, sprint;
        private double buffer;
        private int ticks;
        private static final double[] moveValues = new double[] {-0.98, 0, 0.98};

        @Packet
        public void onUseEntity(WrappedInUseEntityPacket packet) {
            if(!useEntity
                    && packet.getAction().equals(WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK)) {
                useEntity = true;
            }
        }

        @Packet
        public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
            if(data.playerInfo.cvb) {
                pvX = data.playerInfo.velocityX;
                pvZ = data.playerInfo.velocityZ;
                ticks = 0;
                debug("pvX=%.2f pvZ=%.2f", pvX, pvZ);
                data.playerInfo.cvb = false;
            }
            if((pvX != 0 || pvZ != 0) && (data.playerInfo.deltaX != 0
                    || data.playerInfo.deltaY != 0
                    || data.playerInfo.deltaZ != 0)) {
                boolean found = false;

                double drag = 0.91;

                if(data.blockInfo.blocksNear
                        || data.blockInfo.blocksAbove
                        || data.blockInfo.inLiquid
                        || data.lagInfo.lastPingDrop.isNotPassed(4)
                        || data.lagInfo.lastPacketDrop.isNotPassed(2)) {
                    pvX = pvZ = 0;
                    buffer-= buffer > 0 ? 1 : 0;
                    debug("[%.2f] bn=%s lpd=%s lpacket=%s ba=%s", buffer, data.blockInfo.blocksNear,
                            data.lagInfo.lastPingDrop.getPassed(), data.lagInfo.lastPacketDrop.getPassed(),
                            data.blockInfo.blocksAbove);
                    return;
                } else if(data.playerInfo.doingBlockUpdate) {
                    pvX = pvZ = 0;
                    return;
                }

                if(data.playerInfo.lClientGround) {
                    drag*= data.blockInfo.fromFriction;
                }

                if(useEntity && (sprint || (data.getPlayer().getItemInHand() != null
                        && data.getPlayer().getItemInHand().containsEnchantment(Enchantment.KNOCKBACK)))) {
                    pvX*= 0.6;
                    pvZ*= 0.6;
                }

                double f = 0.16277136 / (drag * drag * drag);
                double f5;

                if (data.playerInfo.lClientGround) {
                    f5 = data.predictionService.aiMoveSpeed * f;
                } else {
                    f5 = sprint ? 0.026f : 0.02f;
                }

                double vX = pvX;
                double vZ = pvZ;
                double vXZ = 0;

                List<Tuple<Double[], Double[]>> predictions = new ArrayList<>();

                double moveStrafe = 0, moveForward = 0;
                for (double forward : moveValues) {
                    for(double strafe : moveValues) {
                        double s2 = strafe;
                        double f2 = forward;

                        moveFlying(s2, f2, f5);

                        predictions.add(new Tuple<>(new Double[]{f2, s2}, new Double[]{pvX, pvZ}));

                        pvX = vX;
                        pvZ = vZ;
                    }
                }

                Optional<Tuple<Double[],Double[]>> velocity = predictions.stream()
                        .filter(tuple -> {
                            double deltaX = Math.abs(tuple.two[0] - data.playerInfo.deltaX);
                            double deltaZ = Math.abs(tuple.two[1] - data.playerInfo.deltaZ);

                            return (deltaX * deltaX + deltaZ * deltaZ) < 0.005;
                        })
                        .min(Comparator.comparing(tuple -> {
                            double deltaX = Math.abs(tuple.two[0] - data.playerInfo.deltaX);
                            double deltaZ = Math.abs(tuple.two[1] - data.playerInfo.deltaZ);

                            return (deltaX * deltaX + deltaZ * deltaZ);
                        }));

                if(!velocity.isPresent()) {
                    double s2 = data.predictionService.strafe;
                    double f2 = data.predictionService.forward;

                    moveStrafe = s2;
                    moveForward = f2;

                    moveFlying(s2, f2, f5);
                } else {
                    found = true;
                    Tuple<Double[],Double[]> tuple = velocity.get();

                    moveForward = tuple.one[0];
                    moveStrafe = tuple.one[1];
                    pvX = tuple.two[0];
                    pvZ = tuple.two[1];
                }

                double ratioX = data.playerInfo.deltaX / pvX, ratioZ = data.playerInfo.deltaZ / pvZ;
                double ratio = (Math.abs(ratioX) + Math.abs(ratioZ)) / 2;

                if((ratio < 0.996) && pvX != 0
                        && pvZ != 0
                        && timeStamp - data.creation > 3000L
                        && data.playerInfo.lastTeleportTimer.isPassed(1)
                        && !data.blockInfo.blocksNear) {
                    if(data.playerInfo.lastUseItem.isPassed(2) && ++buffer > 30) {
                        vl++;
                        flag("pct=%.2f buffer=%.1f forward=%.2f strafe=%.2f",
                                ratio * 100, buffer, moveStrafe, moveForward);
                        buffer = 31;
                    }
                } else if(buffer > 0) buffer-= 0.5;

                debug("ratio=%.3f dx=%.4f dz=%.4f buffer=%.1f ticks=%s strafe=%.2f forward=%.2f lastUse=%s " +
                                "found=%s lastV=%s", ratio, data.playerInfo.deltaX, data.playerInfo.deltaZ,
                        buffer, ticks, moveStrafe, moveForward, data.playerInfo.lastUseItem.getPassed(),
                        found, data.playerInfo.lastVelocity.getPassed());

                pvX *= drag;
                pvZ *= drag;

                if(++ticks > 6) {
                    ticks = 0;
                    pvX = pvZ = 0;
                }

                if(Math.abs(pvX) < 0.005) pvX = 0;
                if(Math.abs(pvZ) < 0.005) pvZ = 0;
            }
            sprint = data.playerInfo.sprinting;
            useEntity = false;
        }

        private void moveFlying(double strafe, double forward, double friction) {
            double f = strafe * strafe + forward * forward;

            if (f >= 1.0E-4F) {
                f = Math.sqrt(f);

                if (f < 1.0F) {
                    f = 1.0F;
                }

                f = friction / f;
                strafe = strafe * f;
                forward = forward * f;
                double f1 = Math.sin(data.playerInfo.to.yaw * Math.PI / 180.0F);
                double f2 = Math.cos(data.playerInfo.to.yaw * Math.PI / 180.0F);
                pvX += (strafe * f2 - forward * f1);
                pvZ += (forward * f2 + strafe * f1);
            }
        }
    }
}
