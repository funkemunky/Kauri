package dev.brighten.anticheat.processing;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.*;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.utils.KLocation;
import dev.brighten.anticheat.utils.MiscUtils;
import dev.brighten.anticheat.utils.MovementUtils;
import dev.brighten.anticheat.utils.VanillaUtils;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.potion.PotionEffectType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class MovementProcessor {

    private static List<Float> sensitivities = Arrays.asList(.50f, .75f, 1f, 1.25f, 1.5f, 1.75f, 2f);

    public static float offset = 16777216L;

    public static void preProcess(ObjectData data, WrappedInFlyingPacket packet) {
        /* Pre Motion Y Prediction */
        //Thing in Minecraft that prevents really large numbers.
        data.playerInfo.lpDeltaY = data.playerInfo.pDeltaY;
        if(Math.abs(data.playerInfo.pDeltaY) < 0.005) {
            data.playerInfo.pDeltaY = 0;
        }

        data.playerInfo.lpDeltaX = data.playerInfo.pDeltaX;
        data.playerInfo.lpDeltaZ = data.playerInfo.pDeltaZ;
        data.playerInfo.lpDeltaXZ = data.playerInfo.pDeltaXZ;
    }

    public static void process(ObjectData data, WrappedInFlyingPacket packet) {
        //We check if it's null and intialize the from and to as equal to prevent large deltas causing false positives since there
        //was no previous from (Ex: delta of 380 instead of 0.45 caused by jump jump in location from 0,0,0 to 380,0,0)
        if(data.playerInfo.from == null) {
            data.playerInfo.from
                    = data.playerInfo.to
                    = new KLocation(packet.getX(), packet.getY(), packet.getZ(), packet.getYaw(), packet.getPitch());
            data.playerInfo.clientGround = packet.isGround(); //When a player logs in, he/she may not move.
        } else {
            data.playerInfo.from = new KLocation(
                    data.playerInfo.to.x,
                    data.playerInfo.to.y,
                    data.playerInfo.to.z,
                    data.playerInfo.to.yaw,
                    data.playerInfo.to.pitch,
                    data.playerInfo.to.timeStamp);
        }

        //We set the to x,y,z like this to prevent inaccurate data input. Because if it isnt a positional packet,
        // it returns getX, getY, getZ as 0.
        if(packet.isPos()) {
            data.playerInfo.to.x = packet.getX();
            data.playerInfo.to.y = packet.getY();
            data.playerInfo.to.z = packet.getZ();
        }

        //We set the yaw and pitch like this to prevent inaccurate data input. Like above, it will return both pitch
        //and yaw as 0 if it isnt a look packet.
        if(packet.isLook()) {
            data.playerInfo.to.yaw = packet.getYaw();
            data.playerInfo.to.pitch = packet.getPitch();
        }

        if(data.playerInfo.breakingBlock) {
            data.playerInfo.lastBrokenBlock.reset();
        }

        data.playerInfo.to.timeStamp = System.currentTimeMillis();

        data.playerInfo.clientGround = packet.isGround();

        //Checking for position changes
        if(data.playerInfo.posLocs.size() > 0) {
            Optional<KLocation> optional = data.playerInfo.posLocs.stream()
                    .filter(loc -> MovementUtils.getHorizontalDistance(loc, data.playerInfo.to) <= 1E-8)
                    .findFirst();

            if(optional.isPresent()) {
                data.playerInfo.serverPos = true;
                data.playerInfo.lastServerPos = System.currentTimeMillis();
                data.playerInfo.posLocs.remove(optional.get());
            } else data.playerInfo.serverPos = false;
        } else data.playerInfo.serverPos = false;

        //Setting boundingBox
        data.box = new BoundingBox(data.playerInfo.to.toVector(), data.playerInfo.to.toVector())
                .grow(0.3f, 0, 0.3f)
                .add(0,0,0,0,1.8f,0);

        data.blockInfo.runCollisionCheck(); //run b4 everything else for use below.

        //Setting the motion delta for use in checks to prevent repeated functions.
        data.playerInfo.lDeltaX = data.playerInfo.deltaX;
        data.playerInfo.lDeltaY = data.playerInfo.deltaY;
        data.playerInfo.lDeltaZ = data.playerInfo.deltaZ;
        data.playerInfo.deltaX = (float) (data.playerInfo.to.x - data.playerInfo.from.x);
        data.playerInfo.deltaY = (float) (data.playerInfo.to.y - data.playerInfo.from.y);
        data.playerInfo.deltaZ = (float) (data.playerInfo.to.z - data.playerInfo.from.z);
        data.playerInfo.lDeltaXZ = data.playerInfo.deltaXZ;
        data.playerInfo.deltaXZ = (float)MathUtils.hypot(data.playerInfo.deltaX, data.playerInfo.deltaZ);

        //Setting the angle delta for use in checks to prevent repeated functions.
        data.playerInfo.lDeltaYaw = data.playerInfo.deltaYaw;
        data.playerInfo.lDeltaPitch = data.playerInfo.deltaPitch;
        data.playerInfo.deltaYaw = MathUtils.getDelta(
                MathUtils.yawTo180F(data.playerInfo.to.yaw),
                MathUtils.yawTo180F(data.playerInfo.from.yaw));
        data.playerInfo.deltaPitch = data.playerInfo.to.pitch - data.playerInfo.from.pitch;

        if(packet.isLook()) {
            data.playerInfo.cinematicYaw = findClosestCinematicYaw(data, data.playerInfo.to.yaw, data.playerInfo.from.yaw);
            data.playerInfo.cinematicPitch = findClosestCinematicPitch(data, data.playerInfo.to.pitch, data.playerInfo.from.pitch);


            if (Float.isNaN(data.playerInfo.cinematicPitch) || Float.isNaN(data.playerInfo.cinematicYaw)) {
                data.playerInfo.yawSmooth.reset();
                data.playerInfo.pitchSmooth.reset();
            }

            data.playerInfo.cinematicModePitch = (MathUtils.getDelta(data.playerInfo.cinematicPitch, data.playerInfo.to.pitch) < 0.4 && Math.abs(data.playerInfo.deltaPitch) > 0.01);

            data.playerInfo.lastYawGCD = data.playerInfo.yawGCD;
            data.playerInfo.yawGCD = MiscUtils.gcd((long) (data.playerInfo.deltaYaw * offset), (long) (data.playerInfo.lDeltaYaw * offset));
            data.playerInfo.lastPitchGCD = data.playerInfo.pitchGCD;
            data.playerInfo.pitchGCD = MiscUtils.gcd((long) (Math.abs(data.playerInfo.deltaPitch) * offset), (long) (Math.abs(data.playerInfo.lDeltaPitch) * offset));
        }

        //Setting fallDistance
        if(!data.playerInfo.serverGround
                && data.playerInfo.deltaY < 0
                && !data.blockInfo.onClimbable
                && !data.blockInfo.inLiquid
                && !data.blockInfo.inWeb) {
            data.playerInfo.fallDistance+= -data.playerInfo.deltaY;
        } else data.playerInfo.fallDistance = 0;

        //Running jump check
        if(!data.playerInfo.clientGround) {
            if(!data.playerInfo.jumped && !data.playerInfo.inAir && data.playerInfo.deltaY > 0) {
               data.playerInfo.jumped = true;
                jump(data);
            } else {
                data.playerInfo.inAir = true;
                data.playerInfo.jumped = false;
            }
        } else data.playerInfo.jumped = data.playerInfo.inAir = false;

        /* General Block Info */

        //Setting if players were on blocks when on ground so it can be used with checks that check air things.
        if(data.playerInfo.serverGround) {
            data.playerInfo.wasOnIce = data.blockInfo.onIce;
            data.playerInfo.wasOnSlime = data.blockInfo.onSlime;
        }

        /* General Ticking */

        //Checking if user is in liquid.
        if(data.blockInfo.inLiquid) {
            data.playerInfo.liquidTicks++;
        } else data.playerInfo.liquidTicks-= data.playerInfo.liquidTicks > 0 ? 1 : 0;

        //Half block ticking (slabs, stairs, bed, cauldron, etc.)
        if(data.blockInfo.onHalfBlock) {
            data.playerInfo.halfBlockTicks++;
        } else data.playerInfo.halfBlockTicks-= data.playerInfo.halfBlockTicks > 0 ? 1 : 0;

        //We dont check if theyre still on ice because this would be useless to checks that check a player in air too.
        if(data.playerInfo.wasOnIce) {
            data.playerInfo.iceTicks++;
        } else data.playerInfo.iceTicks-= data.playerInfo.iceTicks > 0 ? 1 : 0;

        if(data.blockInfo.inWeb) {
            data.playerInfo.webTicks++;
        } else data.playerInfo.webTicks-= data.playerInfo.webTicks > 0 ? 1 : 0;

        if(data.blockInfo.onClimbable) {
            data.playerInfo.climbTicks++;
        } else data.playerInfo.climbTicks-= data.playerInfo.climbTicks > 0 ? 1 : 0;

        if(data.playerInfo.wasOnSlime) {
            data.playerInfo.slimeTicks++;
        } else data.playerInfo.slimeTicks-= data.playerInfo.slimeTicks > 0 ? 1 : 0;

        //Player ground/air positioning ticks.
        if(!data.playerInfo.serverGround) {
            data.playerInfo.airTicks++;
            data.playerInfo.groundTicks = 0;
        } else {
            data.playerInfo.groundTicks++;
            data.playerInfo.airTicks = 0;
        }

        /* General Cancel Booleans */
        boolean hasLevi = data.getPlayer().getActivePotionEffects().size() > 0
                && data.getPlayer().getActivePotionEffects()
                .stream()
                .anyMatch(effect -> effect.getType().toString().contains("LEVI"));

        data.playerInfo.flightCancel = data.playerInfo.canFly
                || data.playerInfo.inCreative
                || hasLevi
                || data.playerInfo.serverPos
                || Kauri.INSTANCE.lastTickLag.hasNotPassed(5);

        data.playerInfo.generalCancel = data.playerInfo.canFly
                || data.playerInfo.inCreative
                || hasLevi
                || data.playerInfo.lastVelocity.hasNotPassed(5 + MathUtils.millisToTicks(data.lagInfo.ping))
                || data.playerInfo.serverPos
                || Kauri.INSTANCE.lastTickLag.hasNotPassed(5);

        /* Motion XZ prediction */
        int precision = String.valueOf((int) Math.abs(data.playerInfo.to.x > data.playerInfo.to.z ? data.playerInfo.to.x : data.playerInfo.to.x)).length();
        precision = 15 - precision;
        double preD = Double.valueOf("1.2E-" + Math.max(3, precision - 5)); // the motion deviates further and further from the coordinates 0 0 0. this value fix this

        double mx = data.playerInfo.deltaX - data.playerInfo.lDeltaX * 0.91f; // mx, mz is an Value to calculate the rotation and the Key of the Player
        double mz = data.playerInfo.deltaZ - data.playerInfo.lDeltaZ * 0.91f;

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

        data.playerInfo.key = key;
        data.playerInfo.strafe = moveS * 0.98f;
        data.playerInfo.forward = moveF * 0.98f;

        if(Math.abs(data.playerInfo.pDeltaX) < 0.005) data.playerInfo.pDeltaX = 0;
        if(Math.abs(data.playerInfo.pDeltaZ) < 0.005) data.playerInfo.pDeltaZ = 0;

        moveEntityWithHeading(data, false);

        if(data.playerInfo.lastAttack.hasNotPassed(0)) {
            data.playerInfo.pDeltaX*= 0.6f;
            data.playerInfo.pDeltaZ*= 0.6f;
        }

        data.playerInfo.pDeltaXZ = (float)MathUtils.hypot(data.playerInfo.pDeltaX, data.playerInfo.pDeltaZ);
        /* Motion Y prediction */

        //Checking for jump movement
        if(data.playerInfo.airTicks == 1
                && data.playerInfo.deltaY > 0
                && (!data.playerInfo.wasOnSlime
                || MathUtils.getDelta(data.playerInfo.deltaY, MovementUtils.getJumpHeight(data.getPlayer())) <
                MathUtils.getDelta(data.playerInfo.deltaY, data.playerInfo.pDeltaY)))
        {
            data.playerInfo.pDeltaY = MovementUtils.getJumpHeight(data.getPlayer());
        }

        float pDeltaY = data.playerInfo.pDeltaY;

        data.playerInfo.prePDeltaY = pDeltaY;
        //Jump math

        //Ladder math
        if(VanillaUtils.isOnLadder(data)) {
            if(data.playerInfo.pDeltaY < -.15) {
                data.playerInfo.pDeltaY = -.15f;
            }

            if(data.playerInfo.sneaking) {
                data.playerInfo.pDeltaY = 0;
            }
        }

        //Checking if in web
        if(data.blockInfo.inWeb) {
           pDeltaY = data.playerInfo.pDeltaY *= 0.05000000074505806D;
        }

        //Checking for collisions.
        BoundingBox box = new BoundingBox(data.playerInfo.from.toVector(), data.playerInfo.from.toVector()).grow(0.3f, 0, 0.3f).add(0,0,0,0,1.8f, 0);
        BoundingBox coordBox = box.addCoord(data.playerInfo.deltaX, data.playerInfo.deltaY, data.playerInfo.deltaZ);
        List<BoundingBox> list = Atlas.getInstance().getBlockBoxManager().getBlockBox()
                .getCollidingBoxes(data.getPlayer().getWorld(), coordBox);

        for (BoundingBox boundingBox : list) {
            data.playerInfo.pDeltaY = boundingBox.calculateYOffset(box, pDeltaY);
        }

        if(data.blockInfo.onSlime) {
            if (data.playerInfo.sneaking) {
                data.playerInfo.pDeltaY = 0;
            } else if (data.playerInfo.pDeltaY < 0) {
                data.playerInfo.pDeltaY = -data.playerInfo.pDeltaY;
            }
        }
        list.clear();
        coordBox = box = null;

        //Setting collisions
        data.playerInfo.collidesVertically= data.playerInfo.pDeltaY != pDeltaY;

        if(data.playerInfo.canFly) {
            data.playerInfo.pDeltaY = data.playerInfo.deltaY;
        }

        //Adding past location
        data.pastLocation.addLocation(data.playerInfo.to.clone());
    }

    public static void postProcess(ObjectData data, WrappedInFlyingPacket packet) {
        /* Post Motion Y Prediction */

        //Post ladder math
        if (data.playerInfo.collidesHorizontally
                && VanillaUtils.isOnLadder(data)
                && data.playerInfo.deltaY > 0
                && (data.playerInfo.climbTicks < 2 || (data.playerInfo.deltaY == data.playerInfo.lDeltaY))) {
            data.playerInfo.pDeltaY = 0.2f;
        }

        data.playerInfo.pDeltaY-= 0.08f;
        data.playerInfo.pDeltaY*= 0.98f;

        moveEntityWithHeading(data, true);
        if(data.playerInfo.serverPos) data.playerInfo.pDeltaX = data.playerInfo.pDeltaY = data.playerInfo.pDeltaZ = 0;
    }

    /* Motion XZ Prediction methods */


    private static void moveEntityWithHeading(ObjectData data, boolean after) {
        float f4 = 0.91F;

        if (data.playerInfo.clientGround) {
            Block below = BlockUtils.getBlock(data.playerInfo.to.toLocation(data.getPlayer().getWorld()).subtract(0, 0.5f, 0));
            f4 = (below != null && below.getType().isSolid() ? ReflectionsUtil.getFriction(below) : 0.68f) * 0.91F;
        }

        if(!after) {
            float f = 0.16277136F / (f4 * f4 * f4);
            float f5;

            if (data.playerInfo.clientGround) {
                f5 = Atlas.getInstance().getBlockBoxManager().getBlockBox().getAiSpeed(data.getPlayer()) * f;
            } else {
                f5 = data.playerInfo.sprinting ? 0.026f : 0.02f;
            }

            moveFlying(data, f5);
        } else {
            data.playerInfo.pDeltaX*= f4;
            data.playerInfo.pDeltaZ*= f4;
        }
    }

    private static void moveFlying(ObjectData data, float friction) {
        float strafe = data.playerInfo.strafe, forward = data.playerInfo.forward;
        float f = strafe * strafe + forward * forward;

        if (f >= 1.0E-4F) {
            f = MathHelper.sqrt_float(f);

            if (f < 1.0F) {
                f = 1.0F;
            }

            f = friction / f;
            strafe = strafe * f;
            forward = forward * f;
            float f1 = MathHelper.sin(MathUtils.yawTo180F(data.playerInfo.from.yaw) * (float) Math.PI / 180.0F);
            float f2 = MathHelper.cos(MathUtils.yawTo180F(data.playerInfo.from.yaw) * (float) Math.PI / 180.0F);
            data.playerInfo.pDeltaX += (double) (strafe * f2 - forward * f1);
            data.playerInfo.pDeltaZ += (double) (forward * f2 + strafe * f1);
        }
    }

    private static void jump(ObjectData data) {
        data.playerInfo.lpDeltaY = 0.42F;

        int jump = PlayerUtils.getPotionEffectLevel(data.getPlayer(), PotionEffectType.JUMP);
        if (jump > 0) {
            data.playerInfo.lpDeltaY += jump * 0.1F;
        }

        if (data.playerInfo.sprinting) {
            float f = MathUtils.yawTo180F(data.playerInfo.from.yaw) * 0.017453292F;
            data.playerInfo.pDeltaX -= (double) (MathHelper.sin(f) * 0.2F);
            data.playerInfo.pDeltaZ += (double) (MathHelper.cos(f) * 0.2F);
        }
    }



    /* Cinematic Yaw Methods */

    private static float findClosestCinematicYaw(ObjectData data, float yaw, float lastYaw) {
        float value = sensitivities.stream().min(Comparator.comparing(val -> {
            float f = val * 0.6f + .2f;
            float f1 = (f * f * f) * 8f;
            float smooth = data.playerInfo.mouseFilterX.smooth(lastYaw, 0.05f * f1);
            data.playerInfo.mouseFilterX.reset();
            return MathUtils.getDelta(MathUtils.yawTo180F(yaw), MathUtils.yawTo180F(smooth));
        }, Comparator.naturalOrder())).orElse(1f);

        float f = value * 0.6f + .2f;
        float f1 = (f * f * f) * 8f;
        return data.playerInfo.yawSmooth.smooth(lastYaw, 0.05f * f1);
    }

    private static float findClosestCinematicPitch(ObjectData data, float pitch, float lastPitch) {
        float value = sensitivities.stream().min(Comparator.comparing(val -> {
            float f = val * 0.6f + .2f;
            float f1 = (f * f * f) * 8f;
            float smooth = data.playerInfo.mouseFilterY.smooth(lastPitch, 0.05f * f1);
            data.playerInfo.mouseFilterY.reset();
            return MathUtils.getDelta(pitch, smooth);
        }, Comparator.naturalOrder())).orElse(1f);

        float f = value * 0.6f + .2f;
        float f1 = (f * f * f) * 8f;
        return data.playerInfo.pitchSmooth.smooth(lastPitch, 0.05f * f1);
    }

}
