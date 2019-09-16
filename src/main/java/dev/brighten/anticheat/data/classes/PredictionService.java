package dev.brighten.anticheat.data.classes;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import cc.funkemunky.api.utils.*;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.utils.MovementUtils;
import org.bukkit.potion.PotionEffectType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;

public class PredictionService {

    private ObjectData data;
    public double motionX, motionY, motionZ;
    public float moveStrafing, moveForward;
    public String key;
    public BoundingBox box = new BoundingBox(0,0,0,0,0,0);

    public PredictionService(ObjectData data) {
        this.data = data;
    }

    public void pre(WrappedInFlyingPacket packet) {
        if(data.playerInfo.to == null) return;

        if (Math.abs(this.motionY) < 0.005D) {
            this.motionY = 0.0D;
        }

        int precision = String.valueOf((int) Math.abs(data.playerInfo.to.x > data.playerInfo.to.z ? data.playerInfo.to.x : data.playerInfo.to.x)).length();
        precision = 15 - precision;
        double preD = Double.parseDouble("1.2E-" + Math.max(3, precision - 5)); // the motion deviates further and further from the coordinates 0 0 0. this value fix this

        double mx = data.playerInfo.deltaX - data.playerInfo.lDeltaX * (data.playerInfo.serverGround ? MovementUtils.getFriction(data) * 0.91f : 0.91f); // mx, mz is an Value to calculate the rotation and the Key of the Player
        double mz = data.playerInfo.deltaZ - data.playerInfo.lDeltaZ * (data.playerInfo.serverGround ? MovementUtils.getFriction(data) * 0.91f : 0.91f);

        float motionYaw = (float) (Math.atan2(mz, mx) * 180.0D / Math.PI) - 90.0F;

        int direction = 6;

        motionYaw -= data.playerInfo.to.yaw;

        while (motionYaw > 360.0F)
            motionYaw -= 360.0F;
        while (motionYaw < 0.0F)
            motionYaw += 360.0F;

        motionYaw /= 45.0F; // converts the rotationYaw of the Motion to integers to get keys

        float moveS = 0.0F; // is like the ClientSide moveStrafing moveForward
        float moveF = 0.0F;
        String key = "Nothing";

        if(Math.abs(Math.abs(mx) + Math.abs(mz)) > preD) {
            direction = (int) new BigDecimal(motionYaw).setScale(1, RoundingMode.HALF_UP).doubleValue();
            if (direction == 1) {
                moveF = 1F;
                moveS = -1F;
                key = "W + D";
            } else if (direction == 2) {
                moveS = -1F;
                key = "D";
            } else if (direction == 3) {
                moveF = -1F;
                moveS = -1F;
                key = "S + D";
            } else if (direction == 4) {
                moveF = -1F;
                key = "S";
            } else if (direction == 5) {
                moveF = -1F;
                moveS = 1F;
                key = "S + A";
            } else if (direction == 6) {
                moveS = 1F;
                key = "A";
            } else if (direction == 7) {
                moveF = 1F;
                moveS = 1F;
                key = "W + A";
            } else if (direction == 8) {
                moveF = 1F;
                key = "W";
            } else if (direction == 0) {
                moveF = 1F;
                key = "W";
            }
        }

        if(data.playerInfo.jumped) jump();

        this.key = key;
        moveStrafing = moveS;
        moveForward = moveF;

        moveStrafing*= 0.98f;
        moveForward*= 0.98f;
    }

    public void move(WrappedInFlyingPacket packet) {
        if(data.playerInfo.collidesVertically) motionY = 0;

        if(!data.playerInfo.isFlying) {
            if(data.playerInfo.onLadder) {
                if(data.playerInfo.sneaking && motionY < 0) {
                    motionY = 0;
                } else if(motionY < -0.15) {
                    motionY = -.15;
                }
            }

            if(data.blockInfo.inWeb) {
                motionY *= 0.05000000074505806D;
            }
        } else motionY = data.playerInfo.deltaY;
    }

    public void velocity(WrappedOutVelocityPacket packet) {
        Atlas.getInstance().getSchedular().schedule(() -> motionY = (float)packet.getY(), data.lagInfo.transPing, TimeUnit.MILLISECONDS);
    }

    public void useEntity(WrappedInUseEntityPacket packet) {

    }
    public void post(WrappedInFlyingPacket packet) {
        if(data.playerInfo.isFlying) {
            if(!data.blockInfo.inLiquid) {
                if(data.blockInfo.inWeb) motionY = 0;

                //TODO Check for proper isCollidedHorizontally once proper prediction is finished.
                if(data.blockInfo.onClimbable && data.playerInfo.deltaY > 0) {
                    motionY = 0.2f;
                }

                if(Atlas.getInstance().getBlockBoxManager().getBlockBox().isChunkLoaded(data.getPlayer().getLocation())) {
                    motionY-= 0.08;
                } else {
                    if(data.playerInfo.to.y > 0) {
                        motionY = -.1;
                    } else motionY = 0;
                }
                motionY*= 0.9800000190734863D;
            } else if(data.blockInfo.inWater) {
                motionY *= 0.800000011920929D;

                //TODO proper isCollidedHorizontally check and isOffsetPositionInLiquid.
                if(data.playerInfo.deltaY > 0) {
                    motionY = 0.30000001192092896D;
                }
            } else if(data.blockInfo.inLava) {
                motionY*= 0.5;

                //TODO proper isCollidedHorizontally check and isOffsetPositionInLiquid.
                if(data.playerInfo.deltaY > 0) {
                    motionY = 0.30000001192092896D;
                }
            }
        } else motionY = data.playerInfo.deltaY;
    }

    private void jump() {
        this.motionY = 0.42;

        if (data.getPlayer().hasPotionEffect(PotionEffectType.JUMP)) {
            this.motionY += (double) (PlayerUtils.getPotionEffectLevel(data.getPlayer(), PotionEffectType.JUMP) * 0.1F);
        }
    }
}
