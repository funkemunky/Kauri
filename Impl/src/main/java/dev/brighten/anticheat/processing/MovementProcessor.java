package dev.brighten.anticheat.processing;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.reflection.MinecraftReflection;
import cc.funkemunky.api.tinyprotocol.api.TinyProtocolHandler;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutKeepAlivePacket;
import cc.funkemunky.api.utils.*;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.utils.MiscUtils;
import dev.brighten.anticheat.utils.MovementUtils;
import lombok.RequiredArgsConstructor;
import org.bukkit.GameMode;

import java.math.RoundingMode;

@RequiredArgsConstructor
public class MovementProcessor {
    private final ObjectData data;

    public EvictingList<Float> yawGcdList = new EvictingList<>(50);
    public long deltaX, deltaY, lastDeltaX, lastDeltaY;
    public float sensitivityX, sensitivityY, yawMode, pitchMode;
    public EvictingList<Float> pitchGcdList = new EvictingList<>(50);
    public TickTimer lastEquals = new TickTimer(6);
    private TickTimer lastReset = new TickTimer(1);

    public static float offset = 16777216L;

    public void process(WrappedInFlyingPacket packet, long timeStamp) {
        //We check if it's null and intialize the from and to as equal to prevent large deltas causing false positives since there
        //was no previous from (Ex: delta of 380 instead of 0.45 caused by jump jump in location from 0,0,0 to 380,0,0)
        if (data.playerInfo.from == null) {
            data.playerInfo.from
                    = data.playerInfo.to
                    = new KLocation(packet.getX(), packet.getY(), packet.getZ(), packet.getYaw(), packet.getPitch());
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
        if (packet.isPos()) {
            data.playerInfo.to.x = packet.getX();
            data.playerInfo.to.y = packet.getY();
            data.playerInfo.to.z = packet.getZ();
        }

        data.playerInfo.to.timeStamp = timeStamp;

        data.playerInfo.inVehicle = data.getPlayer().getVehicle() != null;
        data.playerInfo.gliding = PlayerUtils.isGliding(data.getPlayer());
        data.playerInfo.riptiding = Atlas.getInstance().getBlockBoxManager()
                .getBlockBox().isRiptiding(data.getPlayer());

        /* We only set the jumpheight on ground since there's no need to check for it while they're in the air.
        * If we did check while it was in the air, there would be false positives in the checks that use it. */
        if(packet.isGround()) data.playerInfo.jumpHeight = MovementUtils.getJumpHeight(data.getPlayer());

        data.playerInfo.lworldLoaded = data.playerInfo.worldLoaded;

        /* Here we is where we check if the world has loaded for the player. */
        if(Atlas.getInstance().getBlockBoxManager().getBlockBox()
                .isChunkLoaded(data.playerInfo.to.toLocation(data.getPlayer().getWorld()))) {
            if(!data.playerInfo.lworldLoaded) {
                if(data.playerInfo.loadedPacketReceived) {
                    //Sending a keepAlive as confirmation the world was loaded on the player's side.
                    //This prevents false positives caused by high latency or a lag spike.
                    TinyProtocolHandler.sendPacket(data.getPlayer(),
                            new WrappedOutKeepAlivePacket(201).getObject());
                    data.playerInfo.lastLoadedPacketSend.reset();
                    data.playerInfo.loadedPacketReceived = false;
                }
            }
        } else if(data.playerInfo.lworldLoaded) {
            if(data.playerInfo.loadedPacketReceived) {
                //Sending a keepAlive as confirmation the world was loaded on the player's side.
                //This prevents false positives caused by high latency or a lag spike.
                TinyProtocolHandler.sendPacket(data.getPlayer(),
                        new WrappedOutKeepAlivePacket(200).getObject());
                data.playerInfo.lastLoadedPacketSend.reset();
                data.playerInfo.loadedPacketReceived = false;
            }
        }

        data.lagInfo.lagging = data.lagInfo.lastPacketDrop.hasNotPassed(3)
                || !data.playerInfo.worldLoaded
                || Kauri.INSTANCE.lastTickLag.hasNotPassed(20);

        //We set the yaw and pitch like this to prevent inaccurate data input. Like above, it will return both pitch
        //and yaw as 0 if it isnt a look packet.
        if (packet.isLook()) {
            data.playerInfo.to.yaw = packet.getYaw();
            data.playerInfo.to.pitch = packet.getPitch();

            float yawGcd = MiscUtils.gcd((long)(MathUtils.yawTo180F(data.playerInfo.deltaYaw) * offset),
                    (long)(MathUtils.yawTo180F(data.playerInfo.lDeltaYaw) * offset)) / offset;
            float pitchGcd = MiscUtils.gcd((long)Math.abs(data.playerInfo.deltaPitch * offset),
                    (long)Math.abs(data.playerInfo.lDeltaPitch * offset)) / offset;

            //Adding gcd of yaw and pitch.
            if(data.playerInfo.yawGCD > 90000 && yawGcd > 0.01f && data.playerInfo.deltaYaw < 8) yawGcdList.add(yawGcd);
            if(data.playerInfo.pitchGCD > 90000 && data.playerInfo.deltaPitch < 8) pitchGcdList.add(pitchGcd);

            if(yawGcdList.size() > 3 && pitchGcdList.size() > 3) {

                //Making sure to get shit within the std for a more accurate result.
                if(lastReset.hasPassed()) {
                    yawMode = MathUtils.getMode(yawGcdList);
                    pitchMode = MathUtils.getMode(pitchGcdList);
                    lastReset.reset();
                }


                lastDeltaX = deltaX;
                lastDeltaY = deltaY;
                deltaX = getDeltaX(data.playerInfo.deltaYaw, yawMode);
                deltaY = getDeltaY(data.playerInfo.deltaPitch, pitchMode);
                sensitivityX = getSensitivityFromYawGCD(yawMode);
                sensitivityY = getSensitivityFromPitchGCD(pitchMode);

                if (sensToPercent(sensitivityY) == sensToPercent(sensitivityY)) lastEquals.reset();
            }
        }

        /* Velocity Handler */
        if(timeStamp - data.playerInfo.lastVelocityTimestamp < 50L) {
            data.playerInfo.takingVelocity = true;
            data.playerInfo.mvx = data.playerInfo.velocityX;
            data.playerInfo.mvy = data.playerInfo.velocityY;
            data.playerInfo.mvz = data.playerInfo.velocityZ;
        }

        //We use a boolean since it allows for easier management of this handler, and it also isn't dependant on time
        //If it was dependant on time, a player could be taking large amounts of velocity still and it would stop checking.
        //That would most likely cause a false positive.
        if(data.playerInfo.takingVelocity) {
            float drag = data.playerInfo.serverGround ? MovementUtils.getFriction(data) : 0.91f;

            data.playerInfo.mvx*= drag;
            data.playerInfo.mvz*= drag;

            if(!data.playerInfo.serverGround) {
                data.playerInfo.mvy-= 0.08f;
                data.playerInfo.mvy*= 0.98;
                data.playerInfo.mvy = 0;
            } else data.playerInfo.mvy = 0;

            if(MathUtils.hypot(data.playerInfo.mvx, data.playerInfo.mvz) < data.playerInfo.deltaXZ - 0.001) {
                data.playerInfo.takingVelocity = false;
                data.playerInfo.mvx = data.playerInfo.mvz = 0;
            }
        }

        //Fixes glitch when logging in.
        //We use the NMS (bukkit) version since their state is likely saved in a player data file in the world.
        //This should prevent false positives from ability inaccuracies.
        if(timeStamp - data.creation < 500L) {
            if(data.playerInfo.canFly != data.getPlayer().getAllowFlight()) {
                data.playerInfo.lastToggleFlight.reset();
            }
            data.playerInfo.canFly = data.getPlayer().getAllowFlight();
            data.playerInfo.flying = data.getPlayer().isFlying();
            data.playerInfo.creative = data.getPlayer().getGameMode().equals(GameMode.CREATIVE);
        }

        if (data.playerInfo.breakingBlock) data.playerInfo.lastBrokenBlock.reset();

        data.playerInfo.lClientGround = data.playerInfo.clientGround;
        data.playerInfo.clientGround = packet.isGround();

        //We create a separate from BoundingBox for the predictionService since it should operate on pre-motion data.
        data.predictionService.box = new SimpleCollisionBox(data.playerInfo.from.toVector(), 0.6, 1.8).toBoundingBox();
        data.box = new SimpleCollisionBox(data.playerInfo.to.toVector(), 0.6, 1.8);

        data.blockInfo.runCollisionCheck(); //run b4 everything else for use below.

        //Setting the motion delta for use in checks to prevent repeated functions.
        data.playerInfo.lDeltaX = data.playerInfo.deltaX;
        data.playerInfo.lDeltaY = data.playerInfo.deltaY;
        data.playerInfo.lDeltaZ = data.playerInfo.deltaZ;
        data.playerInfo.deltaX = (float) (data.playerInfo.to.x - data.playerInfo.from.x);
        data.playerInfo.deltaY = (float) (data.playerInfo.to.y - data.playerInfo.from.y);
        data.playerInfo.deltaZ = (float) (data.playerInfo.to.z - data.playerInfo.from.z);
        data.playerInfo.lDeltaXZ = data.playerInfo.deltaXZ;
        data.playerInfo.deltaXZ = MathUtils.hypot(data.playerInfo.deltaX, data.playerInfo.deltaZ);

        //Setting the angle delta for use in checks to prevent repeated functions.
        data.playerInfo.lDeltaYaw = data.playerInfo.deltaYaw;
        data.playerInfo.lDeltaPitch = data.playerInfo.deltaPitch;
        data.playerInfo.deltaYaw = MathUtils.getAngleDelta(data.playerInfo.to.yaw, data.playerInfo.from.yaw);
        data.playerInfo.deltaPitch = data.playerInfo.to.pitch - data.playerInfo.from.pitch;

        if (packet.isLook()) {
            data.playerInfo.lCinematicYaw = data.playerInfo.cinematicYaw;
            data.playerInfo.lCinematicPitch = data.playerInfo.cinematicPitch;
            data.playerInfo.cinematicYaw = findClosestCinematicYaw(data, data.playerInfo.to.yaw, data.playerInfo.from.yaw);
            data.playerInfo.cinematicPitch = findClosestCinematicPitch(data, data.playerInfo.to.pitch, data.playerInfo.from.pitch);

            data.playerInfo.cDeltaYaw = MathUtils.getAngleDelta(data.playerInfo.cinematicYaw, data.playerInfo.lCinematicYaw);
            data.playerInfo.cDeltaPitch = MathUtils.getAngleDelta(data.playerInfo.cinematicPitch, data.playerInfo.lCinematicPitch);

            data.playerInfo.cinematicModeYaw = data.playerInfo.yawGCD < 1E6 && deltaX > 4 && lastDeltaX > 4
                    && MathUtils.getDelta(deltaX, lastDeltaX) < Math.min(7, deltaX / (sensitivityX * 12f));

            data.playerInfo.cinematicModePitch = data.playerInfo.pitchGCD < 1E6 && deltaY > 4 && lastDeltaY > 4
                    && MathUtils.getDelta(deltaY, lastDeltaY) < Math.min(7, deltaY / (sensitivityY * 12f));

            if (Float.isNaN(data.playerInfo.cinematicPitch) || Float.isNaN(data.playerInfo.cinematicYaw)) {
                data.playerInfo.yawSmooth.reset();
                data.playerInfo.pitchSmooth.reset();
            }

            data.playerInfo.lastYawGCD = data.playerInfo.yawGCD;
            data.playerInfo.yawGCD = MiscUtils.gcd((long) (data.playerInfo.deltaYaw * offset), (long) (data.playerInfo.lDeltaYaw * offset));
            data.playerInfo.lastPitchGCD = data.playerInfo.pitchGCD;
            data.playerInfo.pitchGCD = MiscUtils.gcd((long) (Math.abs(data.playerInfo.deltaPitch) * offset), (long) (Math.abs(data.playerInfo.lDeltaPitch) * offset));
        }

        data.playerInfo.usingItem = data.getPlayer().isBlocking() ||  Atlas.getInstance().getBlockBoxManager().getBlockBox().isUsingItem(data.getPlayer());

        //Setting fallDistance
        if (!data.playerInfo.serverGround
                && data.playerInfo.deltaY < 0
                && !data.blockInfo.onClimbable
                && !data.blockInfo.inLiquid
                && !data.blockInfo.inWeb) {
            data.playerInfo.fallDistance += -data.playerInfo.deltaY;
        } else data.playerInfo.fallDistance = 0;

        //Running jump check
        if (!data.playerInfo.clientGround) {
            if (!data.playerInfo.jumped && !data.playerInfo.inAir && data.playerInfo.deltaY > 0) {
                data.playerInfo.jumped = true;
            } else {
                data.playerInfo.inAir = true;
                data.playerInfo.jumped = false;
            }
        } else data.playerInfo.jumped = data.playerInfo.inAir = false;

        /* General Block Info */

        //Setting if players were on blocks when on ground so it can be used with checks that check air things.
        if (data.playerInfo.serverGround) {
            data.playerInfo.wasOnIce = data.blockInfo.onIce;
            data.playerInfo.wasOnSlime = data.blockInfo.onSlime;
        }

        /* General Ticking */

        if(data.playerInfo.worldLoaded) {
            data.playerInfo.blockOnTo = data.playerInfo.to.toLocation(data.getPlayer().getWorld()).getBlock();
            data.playerInfo.blockBelow = data.playerInfo.to.toLocation(data.getPlayer().getWorld())
                    .subtract(0, 1, 0).getBlock();

            data.blockInfo.currentFriction = MinecraftReflection.getFriction(data.playerInfo.blockBelow);
        } else data.playerInfo.blockOnTo = data.playerInfo.blockBelow = null;

        data.playerInfo.onLadder = data.playerInfo.blockOnTo != null
                && BlockUtils.isClimbableBlock(data.playerInfo.blockBelow);

        //Checking if user is in liquid.
        if (data.blockInfo.inLiquid) {
            data.playerInfo.liquidTicks++;
        } else data.playerInfo.liquidTicks -= data.playerInfo.liquidTicks > 0 ? 1 : 0;

        //Half block ticking (slabs, stairs, bed, cauldron, etc.)
        if (data.blockInfo.onHalfBlock) {
            data.playerInfo.halfBlockTicks+= 2;
        } else data.playerInfo.halfBlockTicks -= data.playerInfo.halfBlockTicks > 0 ? 1 : 0;

        //We dont check if theyre still on ice because this would be useless to checks that check a player in air too.
        if (data.playerInfo.wasOnIce) {
            data.playerInfo.iceTicks++;
        } else data.playerInfo.iceTicks -= data.playerInfo.iceTicks > 0 ? 1 : 0;

        if (data.blockInfo.inWeb) {
            data.playerInfo.webTicks++;
        } else data.playerInfo.webTicks -= data.playerInfo.webTicks > 0 ? 1 : 0;

        if (data.blockInfo.onClimbable) {
            data.playerInfo.climbTicks++;
        } else data.playerInfo.climbTicks -= data.playerInfo.climbTicks > 0 ? 1 : 0;

        if (data.playerInfo.wasOnSlime) {
            data.playerInfo.slimeTicks++;
        } else data.playerInfo.slimeTicks -= data.playerInfo.slimeTicks > 0 ? 1 : 0;

        if (data.blockInfo.onSoulSand) {
            data.playerInfo.soulSandTicks++;
        } else data.playerInfo.soulSandTicks -= data.playerInfo.soulSandTicks > 0 ? 1 : 0;

        if (data.blockInfo.blocksAbove) {
            data.playerInfo.blocksAboveTicks++;
        } else data.playerInfo.blocksAboveTicks -= data.playerInfo.blocksAboveTicks > 0 ? 1 : 0;

        //Player ground/air positioning ticks.
        if (!data.playerInfo.serverGround) {
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
                || data.playerInfo.creative
                || hasLevi
                || data.playerInfo.inVehicle
                || data.playerInfo.webTicks > 0
                || !data.playerInfo.worldLoaded
                || data.playerInfo.blockOnTo == null
                || data.playerInfo.riptiding
                || data.playerInfo.gliding
                || data.playerInfo.lastToggleFlight.hasNotPassed(40)
                || data.playerInfo.liquidTicks > 0
                || data.playerInfo.climbTicks > 0
                || timeStamp - data.creation < 2000
                || data.playerInfo.serverPos;

        data.playerInfo.generalCancel = data.playerInfo.canFly
                || data.playerInfo.creative
                || hasLevi
                || data.playerInfo.riptiding
                || data.playerInfo.gliding
                || data.playerInfo.inVehicle
                || !data.playerInfo.worldLoaded
                || data.playerInfo.lastToggleFlight.hasNotPassed(40)
                || timeStamp - data.creation < 2000
                || data.playerInfo.blockOnTo == null
                || data.playerInfo.serverPos
                || Kauri.INSTANCE.lastTickLag.hasNotPassed(5);

        //Adding past location
        data.pastLocation.addLocation(data.playerInfo.to.clone());
    }



    /* Cinematic Yaw Methods */

    private float findClosestCinematicYaw(ObjectData data, float yaw, float lastYaw) {
        float value = sensitivityX;

        float f = value * 0.6f + .2f;
        float f1 = (f * f * f) * 8f;
        return data.playerInfo.yawSmooth.smooth(lastYaw, 0.05f * f1);
    }

    private float findClosestCinematicPitch(ObjectData data, float pitch, float lastPitch) {
        float value = sensitivityY;

        float f = value * 0.6f + .2f;
        float f1 = (f * f * f) * 8f;
        return data.playerInfo.pitchSmooth.smooth(lastPitch, 0.05f * f1);
    }

    private static int getDeltaX(float yawDelta, float gcd) {
        float f2 = yawToF2(yawDelta);

        return MathUtils.floor(f2 / getF1FromYaw(gcd));
    }

    private static int getDeltaY(float pitchDelta, float gcd) {
        float f3 = pitchToF3(pitchDelta);

        return MathUtils.floor(f3 / getF1FromPitch(gcd));
    }

    public static int sensToPercent(float sensitivity) {
        return (int) MathUtils.round(
                sensitivity / .5f * 100, 0,
                RoundingMode.HALF_UP);
    }

    //TODO Condense. This is just for easy reading until I test everything.
    private static float getSensitivityFromYawGCD(float gcd) {
        float stepOne = yawToF2(gcd) / 8;
        float stepTwo = (float)Math.cbrt(stepOne);
        float stepThree = stepTwo - .2f;
        return stepThree / .6f;
    }

    //TODO Condense. This is just for easy reading until I test everything.
    private static float getSensitivityFromPitchGCD(float gcd) {
        float stepOne = pitchToF3(gcd) / 8;
        float stepTwo = (float)Math.cbrt(stepOne);
        float stepThree = stepTwo - .2f;
        return stepThree / .6f;
    }

    private static float getF1FromYaw(float gcd) {
        float f = getFFromYaw(gcd);

        return (float)Math.pow(f, 3) * 8;
    }

    private static float getFFromYaw(float gcd) {
        float sens = getSensitivityFromYawGCD(gcd);
        return sens * .6f + .2f;
    }

    private static float getFFromPitch(float gcd) {
        float sens = getSensitivityFromPitchGCD(gcd);
        return sens * .6f + .2f;
    }

    private static float getF1FromPitch(float gcd) {
        float f = getFFromPitch(gcd);

        return (float)Math.pow(f, 3) * 8;
    }

    private static float yawToF2(float yawDelta) {
        return yawDelta / .15f;
    }

    private static float pitchToF3(float pitchDelta) {
        int b0 = pitchDelta >= 0 ? 1 : -1; //Checking for inverted mouse.
        return pitchDelta / .15f / b0;
    }

}
