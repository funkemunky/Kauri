package dev.brighten.anticheat.check.impl.movement.velocity;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;

@CheckInfo(name = "Velocity (C)", description = "A simple horizontal velocity check.", checkType = CheckType.VELOCITY,
        punishVL = 80, vlToFlag = 15)
@Cancellable
public class VelocityC extends Check {

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
        }
        if((pvX != 0 || pvZ != 0)) {
            boolean found = false;

            double drag = 0.91;

            //There is a bug in 1.16 where if player stands on edge of block, they will take no horizontal kb.
            if(data.playerInfo.sneaking && data.playerVersion.isOrAbove(ProtocolVersion.v1_16)) {
                pvX = pvZ = 0;
                buffer-= buffer > 0 ? 1 : 0;
                return;
            }

            //All of these blocks being near can cause false positives.
            if(data.blockInfo.blocksNear
                    || data.blockInfo.blocksAbove
                    || data.blockInfo.inLiquid) {
                pvX = pvZ = 0;
                buffer-= buffer > 0 ? 1 : 0;
                return;
            }

            //Setting the block friction factor if they were on the ground like Minecraft does.
            if(data.playerInfo.lClientGround) {
                drag*= data.blockInfo.fromFriction;
            }

            //If player is sprinting or has kb sword, and attacks, then their Minecraft will multiplier their own
            //horizontal movemment on each axis by 0.6. So we update our threshold to compensate.
            if(data.target instanceof HumanEntity
                    && useEntity && (sprint || (data.getPlayer().getItemInHand() != null
                    && data.getPlayer().getItemInHand().containsEnchantment(Enchantment.KNOCKBACK)))) {
                pvX*= 0.6;
                pvZ*= 0.6;
            }

            double f = 0.16277136 / (drag * drag * drag);
            double f5;

            //The minecraft aiSpeed calculation for air and ground.
            if (data.playerInfo.lClientGround) {
                f5 = data.predictionService.aiMoveSpeed * f;
            } else {
                f5 = sprint ? 0.026 : 0.02;
            }

            double vX = pvX;
            double vZ = pvZ;
            double vXZ = 0;

            //Calculating the forward and strafe key presses of the player
            //This won't be 100% accurate but it will result in whatever will get the closest to 100% velocity.
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

                    double deltaX = Math.abs(pvX - data.playerInfo.deltaX);
                    double deltaZ = Math.abs(pvZ - data.playerInfo.deltaZ);

                    pvX = vX;
                    pvZ = vZ;
                    if(deltaX <= 0.005 - (data.playerInfo.usingItem ? 0.0045 : 0)
                            && deltaZ <= 0.005 - (data.playerInfo.usingItem ? 0.0045 : 0)) {
                        moveForward = f2;
                        moveStrafe = s2;
                        found = true;
                        break;
                    }

                }
            }

            //If the calculations above don't find a reasonably close forward strafe calculation, we will default
            //to our prediction processor. We don't just use the prediction processor since it can be unstable. Maybe
            //when it is finally mature, then can we rely on it.
            if(!found) {
                moveStrafe = data.predictionService.moveStrafing;
                moveForward = data.predictionService.moveForward;
                //If player is using an item, their strafe forward calculation will get multiplied by 0.2 by MC.
                if(data.playerInfo.usingItem) {
                    moveStrafe*= 0.2;
                    moveForward*= 0.2;
                }
            }


            moveFlying(moveStrafe, moveForward, f5);

            //Instead of doing a hypot, this will prevent velocity bypasses that go forward or sideways, etc.
            double ratio = (data.playerInfo.deltaX / vX + data.playerInfo.deltaZ / vZ) / 2;

            if((ratio < 0.8)
                    && timeStamp - data.creation > 3000L //We don't want to flag them when they just login
                    && !data.getPlayer().getItemInHand().getType().isEdible() /*this may cause problems even tho we
                    check for use item*/) {
                if(++buffer > 20) { //High buffers make the dream work in preventing false positives.
                    vl++;
                    flag("pct=%.2f% buffer=%.1f forward=%.2f strafe=%.2f",
                            ratio * 100, buffer, moveStrafe, moveForward);
                }
            } else buffer-= buffer > 0 ? data.lagInfo.lastPacketDrop.isNotPassed(20) ? .5 : 0.25 : 0;
            debug("ratio=%.3f buffer=%.1f strafe=%.2f forward=%.2f lastUse=%s found=%s",
                    ratio, buffer, moveStrafe, moveForward, data.playerInfo.lastUseItem.getPassed(), found);
            pvX *= drag;
            pvZ *= drag;

            if(++ticks > 6) { //After 6 ticks of checking velocity, we will stop checking as a just in case.
                ticks = 0;
                pvX = pvZ = 0;
            }

            //Only something that runs in Minecraft clients below the 1.9 release.
            if(data.playerVersion.isBelow(ProtocolVersion.V1_9)) {
                if (Math.abs(pvX) < 0.005) pvX = 0;
                if (Math.abs(pvZ) < 0.005) pvZ = 0;
            }
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