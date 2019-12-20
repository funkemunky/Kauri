package dev.brighten.anticheat.check.impl.movement.speed;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInKeepAlivePacket;
import cc.funkemunky.api.utils.MathHelper;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.TickTimer;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import lombok.val;

@CheckInfo(name = "Speed (D)", description = "Ensures the acceleration of a player is legitimate.",
        checkType = CheckType.SPEED, developer = true, punishVL = 30)
public class SpeedD extends Check {

    private float mx, mz, lmx, lmz;
    private boolean lsprint;
    private String lastKey = "";
    private TickTimer lastKeyChange = new TickTimer(6);

    @Packet
    public void onKeepAlive(WrappedInKeepAlivePacket packet) {
        if(packet.getTime() == 101) {
            mx = data.playerInfo.velocityX;
            mz = data.playerInfo.velocityZ;
        }
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timestamp) {
        if(packet.isPos()) {
            float drag = 0.91f;

            if(data.playerInfo.lClientGround) {
                drag *= data.blockInfo.currentFriction;
            }

            float f = 0.16277136F / (float)Math.pow(drag, 3);
            float f5;

            if (data.playerInfo.lClientGround) {
                f5 = data.predictionService.aiMoveSpeed * f;

                //Credits to Toon for this
                if (data.playerInfo.sprinting && f5 < 0.129F) {
                    f5 *= 1.3;
                }
            } else {
                f5 = lsprint ? 0.02600001f : 0.0200001f;

                //Credits to Toon for this
                if (data.playerInfo.sprinting && f5 < 0.026) {
                    f5 += 0.006f;
                }
            }

            val moveFlying = moveFlying(
                    new float[]{data.predictionService.moveStrafing, data.predictionService.moveForward},
                    f5);

            mx = moveFlying[0];
            mz = moveFlying[1];

            /*if(data.playerInfo.lClientGround
                    && !data.playerInfo.clientGround
                    && (MathUtils.getDelta(data.playerInfo.jumpHeight, data.playerInfo.deltaY) < 0.01
                    || (data.blockInfo.blocksAbove && data.playerInfo.deltaY > 0 && data.playerInfo.lDeltaY <= 0))
                    && lsprint) {
                float rot = data.playerInfo.to.yaw * 0.017453292F;
                mx -= (double) (MathHelper.sin(rot) * 0.20000000298023224D);
                mz += (double) (MathHelper.cos(rot) * 0.20000000298023224D);
            }*/

            //^ That is not how the client handles rotations in EntityLivingBase#1373 in the client
            if (data.playerInfo.serverGround && data.playerInfo.sprinting && data.playerInfo.deltaY > 0.4199D) {
                float rot = data.playerInfo.to.yaw * 0.017453292F;
                mx -= (MathHelper.sin(rot) * 0.20000000298023224F);
                mz += (MathHelper.cos(rot) * 0.20000000298023224F);
            }

            float mxz = MathUtils.hypot(mx, mz);

            if(!lastKey.equals(data.predictionService.key)) {
                lastKeyChange.reset();
            }

            if(timestamp - data.creation < 1000L) {
                mx = data.playerInfo.deltaX;
                mz = data.playerInfo.deltaZ;
            }

            float threshold = mxz
                    + (lastKeyChange.hasNotPassed() || lastKey.equals("Nothing") ? 0.1f : 0.005f);

            if(data.playerInfo.wasOnSlime || data.playerInfo.generalCancel) {
                mx = data.playerInfo.deltaX;
                mz = data.playerInfo.deltaZ;
            }

            if(data.playerInfo.deltaXZ > threshold
                    && data.playerInfo.lastVelocity.hasPassed(3)
                    && !data.playerInfo.generalCancel) {
                vl+= lastKeyChange.hasNotPassed(14) ? 0.2 : 1;
                if(vl > 8 || data.playerInfo.deltaXZ - threshold > 0.7) {
                    flag(data.playerInfo.deltaXZ + ">-" + mxz);
                }
            } else vl-= vl > 0 ? 0.2 : 0;
            debug("p=" + mxz
                    + " a=" + data.playerInfo.deltaXZ
                    + " key=" + data.predictionService.key
                    + " ground="  + data.playerInfo.lClientGround
                    + " sp=" + lsprint
                    + " delta=" + (data.playerInfo.deltaXZ - mxz)
                    + " vl=" + vl);

            lmx = mx;
            lmz = mz;
            mx*= drag;
            mz*= drag;

        } else mx = mz = 0;
        lsprint = data.playerInfo.sprinting;
        lastKey = data.predictionService.key;
    }

    private float[] moveFlying(float[] array, float friction) {
        if(array.length == 2) {
            float strafe = array[0];
            float forward = array[1];
            float f = strafe * strafe + forward * forward;

            if (f >= 1.0E-4F) {
                f = MathHelper.sqrt_float(f);

                if (f < 1.0F) {
                    f = 1.0F;
                }

                f = friction / f;
                strafe = strafe * f;
                forward = forward * f;
                float f1 = MathHelper.sin(data.playerInfo.to.yaw * (float) Math.PI / 180.0F);
                float f2 = MathHelper.cos(data.playerInfo.to.yaw * (float) Math.PI / 180.0F);
                return new float[]{mx + (strafe * f2 - forward * f1), mz + (forward * f2 + strafe * f1)};
            }
        }
        return new float[]{mx, mz};
    }
}
