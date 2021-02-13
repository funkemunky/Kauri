package dev.brighten.anticheat.premium.impl;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import cc.funkemunky.api.utils.Tuple;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.KauriVersion;
import dev.brighten.api.check.CheckType;
import org.bukkit.enchantments.Enchantment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@CheckInfo(name = "Velocity (B)", description = "A horizontal velocity check.", checkType = CheckType.VELOCITY,
        punishVL = 80, vlToFlag = 15, planVersion = KauriVersion.ARA)
@Cancellable
public class VelocityB extends Check {

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
        if(data.playerInfo.lastVelocity.isNotPassed(0)) {
            pvX = data.playerInfo.velocityX;
            pvZ = data.playerInfo.velocityZ;
            ticks = 0;
        }
        if((pvX != 0 || pvZ != 0)) {
            boolean found = false;

            double drag = 0.91;

            if(data.blockInfo.blocksNear
                    || data.blockInfo.blocksAbove
                    || data.blockInfo.inLiquid
                    || data.lagInfo.lastPingDrop.isNotPassed(10)
                    || data.lagInfo.lastPacketDrop.isNotPassed(10)) {
                pvX = pvZ = 0;
                buffer-= buffer > 0 ? 1 : 0;
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
                f5 = sprint ? 0.026 : 0.02;
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

                    if(data.playerInfo.usingItem) {
                        s2*= 0.2;
                        f2*= 0.2;
                    }

                    moveFlying(s2, f2, f5);

                    predictions.add(new Tuple<>(new Double[]{f2, s2}, new Double[]{pvX, pvZ}));

                    pvX = vX;
                    pvZ = vZ;
                }
            }

            Optional<Tuple<Double[],Double[]>> velocity = predictions.stream()
                    .min(Comparator.comparing(tuple -> {
                double deltaX = Math.abs(tuple.two[0] - data.playerInfo.deltaX);
                double deltaZ = Math.abs(tuple.two[1] - data.playerInfo.deltaZ);

                return Math.hypot(deltaX, deltaZ);
            }));

            if(!velocity.isPresent()) {
                moveStrafe = data.predictionService.moveStrafing;
                moveForward = data.predictionService.moveForward;
                if(data.playerInfo.usingItem) {
                    moveStrafe*= 0.2;
                    moveForward*= 0.2;
                }
                moveFlying(moveStrafe, moveForward, f5);
            } else {
                found = true;
                Tuple<Double[],Double[]> tuple = velocity.get();

                moveForward = tuple.one[0];
                moveStrafe = tuple.one[1];
                pvX = tuple.two[0];
                pvZ = tuple.two[1];
            }

            double ratioX = data.playerInfo.deltaX / pvX, ratioZ = data.playerInfo.deltaZ / pvZ;
            double ratio = (ratioX + ratioZ) / 2;

            if((ratio < 0.998 || ratio > 3) && pvX != 0
                    && pvZ != 0
                    && timeStamp - data.creation > 3000L
                    && !data.getPlayer().getItemInHand().getType().isEdible()
                    && !data.blockInfo.blocksNear) {
                if(++buffer > 30) {
                    vl++;
                    flag("pct=%v.2% buffer=%v.1 forward=%v.2 strafe=%v.2",
                            ratio * 100, buffer, moveStrafe, moveForward);
                }
            } else buffer-= buffer > 0 ? data.lagInfo.lastPacketDrop.isNotPassed(20) ? .5 : 0.25 : 0;

            debug("ratio=%v.3 buffer=%v.1 strafe=%v.2 forward=%v.2 lastUse=%v found=%v lastV=%v",
                    ratio, buffer, moveStrafe, moveForward, data.playerInfo.lastUseItem.getPassed(), found,
                    data.playerInfo.lastVelocity.getPassed());

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