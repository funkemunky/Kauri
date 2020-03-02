package dev.brighten.anticheat.processing;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.reflections.impl.MinecraftReflection;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.*;
import cc.funkemunky.api.utils.objects.VariableValue;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import cc.funkemunky.api.utils.world.types.RayCollision;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.utils.GraphUtil;
import dev.brighten.anticheat.utils.MiscUtils;
import dev.brighten.anticheat.utils.MovementUtils;
import lombok.val;
import org.bukkit.GameMode;
import org.bukkit.potion.PotionEffectType;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MovementProcessor {
    private final ObjectData data;

    public List<Double> yawGcdList = Collections.synchronizedList(new EvictingList<>(50));
    public List<Double> pitchGcdList = Collections.synchronizedList(new EvictingList<>(50));
    public long deltaX, deltaY, lastDeltaX, lastDeltaY, lastCinematic;
    private List<Float> yawList = new ArrayList<>(), pitchList = new ArrayList<>();
    public double sensitivityX, sensitivityY, yawMode, pitchMode;
    public TickTimer lastEquals = new TickTimer(6);
    private TickTimer lastReset = new TickTimer(1);

    public static double offset = Math.pow(2, 24);

    public PotionEffectType levitation = null;

    public MovementProcessor(ObjectData data) {
        this.data = data;

        try {
            levitation = PotionEffectType.getByName("LEVITATION");
        } catch(Exception e) {

        }
    }

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

        if(deltaY > -0.0981 && deltaY < -0.0979)
            cc.funkemunky.api.utils.MiscUtils.printToConsole(data.getPlayer().getName() + ": " + deltaY);

        if (data.playerInfo.posLocs.size() > 0) {
            val optional = data.playerInfo.posLocs.stream()
                    .filter(loc -> loc.toVector().setY(0)
                            .distance(data.playerInfo.to.toVector().setY(0)) <= 1E-8
                            && MathUtils.getDelta(loc.y, data.playerInfo.to.y) < 4)
                    .findFirst();

            if (optional.isPresent()) {
                data.playerInfo.serverPos = true;
                data.playerInfo.lastServerPos = timeStamp;
                data.playerInfo.inventoryOpen = false;
                data.playerInfo.posLocs.remove(optional.get());
            }
        } else if (data.playerInfo.serverPos) {
            data.playerInfo.serverPos = false;
        }

        data.playerInfo.lClientGround = data.playerInfo.clientGround;
        data.playerInfo.clientGround = packet.isGround();
        //Setting the motion delta for use in checks to prevent repeated functions.
        data.playerInfo.lDeltaX = data.playerInfo.deltaX;
        data.playerInfo.lDeltaY = data.playerInfo.deltaY;
        data.playerInfo.lDeltaZ = data.playerInfo.deltaZ;
        data.playerInfo.deltaX = data.playerInfo.to.x - data.playerInfo.from.x;
        data.playerInfo.deltaY = data.playerInfo.to.y - data.playerInfo.from.y;
        data.playerInfo.deltaZ = data.playerInfo.to.z - data.playerInfo.from.z;
        data.playerInfo.lDeltaXZ = data.playerInfo.deltaXZ;
        data.playerInfo.deltaXZ = MathUtils.hypot(data.playerInfo.deltaX, data.playerInfo.deltaZ);

        data.playerInfo.blockOnTo = BlockUtils.getBlock(data.playerInfo.to.toLocation(data.getPlayer().getWorld()));
        data.playerInfo.blockBelow = BlockUtils.getBlock(data.playerInfo.to.toLocation(data.getPlayer().getWorld())
                .subtract(0, 1, 0));

        if(data.playerInfo.blockBelow != null)
            data.blockInfo.currentFriction = MinecraftReflection.getFriction(data.playerInfo.blockBelow);

        if(packet.isPos()) {
            //We create a separate from BoundingBox for the predictionService since it should operate on pre-motion data.
            data.box = new SimpleCollisionBox(data.playerInfo.to.toVector(), 0.6, 1.8);

            data.blockInfo.runCollisionCheck(); //run b4 everything else for use below.
        }

        data.playerInfo.inVehicle = data.getPlayer().getVehicle() != null;
        data.playerInfo.gliding = PlayerUtils.isGliding(data.getPlayer());
        data.playerInfo.riptiding = Atlas.getInstance().getBlockBoxManager()
                .getBlockBox().isRiptiding(data.getPlayer());

        /* We only set the jumpheight on ground since there's no need to check for it while they're in the air.
         * If we did check while it was in the air, there would be false positives in the checks that use it. */
        if (packet.isGround()) {
            data.playerInfo.jumpHeight = MovementUtils.getJumpHeight(data.getPlayer());
        }

        if(data.playerInfo.clientGround || data.playerInfo.lClientGround) {
            data.playerInfo.totalHeight = MovementUtils.getTotalHeight(data.playerVersion,
                    (float)data.playerInfo.jumpHeight);
        }

        data.playerInfo.lworldLoaded = data.playerInfo.worldLoaded;

        data.lagInfo.lagging = data.lagInfo.lagTicks.subtract() > 0
                || !data.playerInfo.worldLoaded
                || timeStamp - Kauri.INSTANCE.lastTick >
                new VariableValue<>(110, 60, ProtocolVersion::isPaper).get();

        if(data.playerInfo.insideBlock = (data.playerInfo.blockOnTo != null && data.playerInfo.blockOnTo.getType()
                .equals(XMaterial.AIR.parseMaterial()))) {
            data.playerInfo.lastInsideBlock.reset();
        }

        //We set the yaw and pitch like this to prevent inaccurate data input. Like above, it will return both pitch
        //and yaw as 0 if it isnt a look packet.
        if (packet.isLook()) {
            data.playerInfo.to.yaw = packet.getYaw();
            data.playerInfo.to.pitch = packet.getPitch();

            double yawGcd = MiscUtils.gcd((long) (MathUtils.yawTo180F(data.playerInfo.deltaYaw) * offset),
                    (long) (MathUtils.yawTo180F(data.playerInfo.lDeltaYaw) * offset)) / offset;
            double pitchGcd = MiscUtils.gcd((long) Math.abs(data.playerInfo.deltaPitch * offset),
                    (long) Math.abs(data.playerInfo.lDeltaPitch * offset)) / offset;

            //Adding gcd of yaw and pitch.
            if (data.playerInfo.yawGCD > 90000 && data.playerInfo.yawGCD < 2E7
                    && yawGcd > 0.01f && data.playerInfo.deltaYaw < 8)
                yawGcdList.add(yawGcd);
            if (data.playerInfo.pitchGCD > 90000 && data.playerInfo.pitchGCD < 2E7
                    && Math.abs(data.playerInfo.deltaPitch) < 8) pitchGcdList.add(pitchGcd);

            if (yawGcdList.size() > 3 && pitchGcdList.size() > 3) {

                //Making sure to get shit within the std for a more accurate result.
                if (lastReset.hasPassed()) {
                    yawMode = MathUtils.getMode(yawGcdList);
                    pitchMode = MathUtils.getMode(pitchGcdList);
                    lastReset.reset();
                }


                lastDeltaX = deltaX;
                lastDeltaY = deltaY;
                deltaX = getDeltaX(data.playerInfo.deltaYaw, (float) yawMode);
                deltaY = getDeltaY(data.playerInfo.deltaPitch, (float) pitchMode);
                sensitivityX = getSensitivityFromYawGCD(yawMode);
                sensitivityY = getSensitivityFromPitchGCD(pitchMode);

                if (sensToPercent(sensitivityY) == sensToPercent(sensitivityY)) lastEquals.reset();
            }
        }

        /* Velocity Handler */
        if (timeStamp - data.playerInfo.lastVelocityTimestamp < 50L) {
            data.playerInfo.takingVelocity = true;
            data.playerInfo.mvx = data.playerInfo.velocityX;
            data.playerInfo.mvy = data.playerInfo.velocityY;
            data.playerInfo.mvz = data.playerInfo.velocityZ;
        }

        //We use a boolean since it allows for easier management of this handler, and it also isn't dependant on time
        //If it was dependant on time, a player could be taking large amounts of velocity still and it would stop checking.
        //That would most likely cause a false positive.
        if (data.playerInfo.takingVelocity) {
            float drag = data.playerInfo.serverGround ? MovementUtils.getFriction(data) : 0.91f;

            data.playerInfo.mvx *= drag;
            data.playerInfo.mvz *= drag;

            if (!data.playerInfo.serverGround) {
                data.playerInfo.mvy -= 0.08f;
                data.playerInfo.mvy *= 0.98;
                data.playerInfo.mvy = 0;
            } else data.playerInfo.mvy = 0;

            if (MathUtils.hypot(data.playerInfo.mvx, data.playerInfo.mvz) < data.playerInfo.deltaXZ - 0.001) {
                data.playerInfo.takingVelocity = false;
                data.playerInfo.mvx = data.playerInfo.mvz = 0;
            }
        }

        if (packet.isPos()) {
            if (data.playerInfo.serverGround && data.playerInfo.groundTicks > 4)
                data.playerInfo.groundLoc = data.playerInfo.to;
        }

        //Fixes glitch when logging in.
        //We use the NMS (bukkit) version since their state is likely saved in a player data file in the world.
        //This should prevent false positives from ability inaccuracies.
        if (timeStamp - data.creation < 500L) {
            if (data.playerInfo.canFly != data.getPlayer().getAllowFlight()) {
                data.playerInfo.lastToggleFlight.reset();
            }
            data.playerInfo.canFly = data.getPlayer().getAllowFlight();
            data.playerInfo.flying = data.getPlayer().isFlying();
            data.playerInfo.creative = data.getPlayer().getGameMode().equals(GameMode.CREATIVE);
        }

        if (data.playerInfo.breakingBlock) data.playerInfo.lastBrokenBlock.reset();

        //Setting the angle delta for use in checks to prevent repeated functions.
        data.playerInfo.lDeltaYaw = data.playerInfo.deltaYaw;
        data.playerInfo.lDeltaPitch = data.playerInfo.deltaPitch;
        data.playerInfo.deltaYaw = MathUtils.getDelta(data.playerInfo.to.yaw, data.playerInfo.from.yaw);
        data.playerInfo.deltaPitch = data.playerInfo.to.pitch - data.playerInfo.from.pitch;

        if (packet.isLook()) {
            yawList.add(data.playerInfo.deltaYaw / MovementProcessor.sensToPercent(sensitivityX));
            pitchList.add(data.playerInfo.deltaPitch / MovementProcessor.sensToPercent(sensitivityY));
            if (this.yawList.size() == 20 && this.pitchList.size() == 20) {
                // Get the negative/positive graph of the sample-lists
                GraphUtil.GraphResult yawResults = GraphUtil.getGraph(yawList);
                GraphUtil.GraphResult pitchResults = GraphUtil.getGraph(pitchList);

                // Negative values
                int yawNegatives = yawResults.getNegatives();
                int pitchNegatives = pitchResults.getNegatives();

                // Positive values
                int yawPositives = yawResults.getPositives();
                int pitchPositives = pitchResults.getPositives();

                // Cinematic camera usually does this on *most* speeds and is accurate for the most part.
                if (yawPositives > yawNegatives || pitchPositives > pitchNegatives) {
                    lastCinematic = timeStamp;
                }

                data.playerInfo.cinematicMode = timeStamp - lastCinematic < 400L;

                MiscUtils.testMessage(yawPositives + ", " + pitchPositives);

                // Clear the sample-lists
                pitchList.clear();
                yawList.clear();
            }

            if (Float.isNaN(data.playerInfo.cinematicPitch) || Float.isNaN(data.playerInfo.cinematicYaw)) {
                data.playerInfo.yawSmooth.reset();
                data.playerInfo.pitchSmooth.reset();
            }

            data.playerInfo.lastYawGCD = data.playerInfo.yawGCD;
            data.playerInfo.yawGCD = MiscUtils.gcd((long) (data.playerInfo.deltaYaw * offset),
                    (long) (data.playerInfo.lDeltaYaw * offset));
            data.playerInfo.lastPitchGCD = data.playerInfo.pitchGCD;
            data.playerInfo.pitchGCD = MiscUtils.gcd((long) (Math.abs(data.playerInfo.deltaPitch) * offset),
                    (long) (Math.abs(data.playerInfo.lDeltaPitch) * offset));

            val origin = data.playerInfo.to.clone();

            origin.y+= data.playerInfo.sneaking ? 1.54 : 1.62;
            RayCollision collision = new RayCollision(origin.toVector(), MathUtils.getDirection(origin));

            List<SimpleCollisionBox> boxes = new ArrayList<>();
            collision.boxesOnRay(data.getPlayer().getWorld(), data.playerInfo.creative ? 7.5 : 5.5).forEach(box -> box.downCast(boxes));

            data.playerInfo.lookingAtBlock = boxes.size() > 0;
        }

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
        if (data.playerInfo.serverGround || data.playerInfo.clientGround || data.playerInfo.collided) {
            data.playerInfo.wasOnIce = data.blockInfo.onIce;
            data.playerInfo.wasOnSlime = data.blockInfo.onSlime;
        }

        if((data.playerInfo.onLadder = MovementUtils.isOnLadder(data))
                && (data.playerInfo.deltaY <= 0 || data.blockInfo.collidesHorizontally)) {
            data.playerInfo.isClimbing = true;
        }

        //Checking if user is in liquid.
        if (data.blockInfo.inLiquid) data.playerInfo.liquidTimer.reset();

        //Half block ticking (slabs, stairs, bed, cauldron, etc.)
        if (data.blockInfo.onHalfBlock) data.playerInfo.lastHalfBlock.reset();

        //We dont check if theyre still on ice because this would be useless to checks that check a player in air too.
        if (data.blockInfo.onIce) data.playerInfo.iceTimer.reset();

        if (data.blockInfo.inWeb) data.playerInfo.webTimer.reset();

        if (data.blockInfo.onClimbable) data.playerInfo.climbTimer.reset();

        if (data.blockInfo.onSlime) data.playerInfo.slimeTimer.reset();

        if (data.blockInfo.onSoulSand) data.playerInfo.soulSandTimer.reset();

        if (data.blockInfo.blocksAbove) data.playerInfo.blockAboveTimer.reset();

        //Player ground/air positioning ticks.
        if (!data.playerInfo.serverGround) {
            data.playerInfo.airTicks++;
            data.playerInfo.groundTicks = 0;
        } else {
            data.playerInfo.groundTicks++;
            data.playerInfo.airTicks = 0;
        }

        data.playerInfo.baseSpeed = MovementUtils.getBaseSpeed(data);
        /* General Cancel Booleans */
        boolean hasLevi = levitation != null && data.getPlayer().hasPotionEffect(levitation);

        data.playerInfo.generalCancel = data.playerInfo.canFly
                || data.playerInfo.creative
                || hasLevi
                || (data.playerInfo.deltaY > -0.0981
                && data.playerInfo.deltaY < -0.0979
                && data.playerInfo.deltaXZ < 0.1)
                || timeStamp - data.playerInfo.lastServerPos < 80L
                || data.playerInfo.riptiding
                || data.playerInfo.gliding
                || data.playerInfo.lastPlaceLiquid.hasNotPassed(5)
                || data.playerInfo.inVehicle
                || data.playerInfo.lastWorldUnload.hasNotPassed(10)
                || !data.playerInfo.worldLoaded
                || data.playerInfo.lastToggleFlight.hasNotPassed(40)
                || timeStamp - data.creation < 2000
                || Kauri.INSTANCE.lastTickLag.hasNotPassed(5);

        data.playerInfo.flightCancel = data.playerInfo.generalCancel
                || data.playerInfo.webTimer.hasNotPassed(4)
                || data.playerInfo.lastPlaceLiquid.hasNotPassed(15)
                || data.playerInfo.liquidTimer.hasNotPassed(6)
                || data.playerInfo.onLadder
                || data.playerInfo.climbTimer.hasNotPassed(4)
                || data.playerInfo.lastHalfBlock.hasNotPassed(2);

        //Adding past location
        data.pastLocation.addLocation(data.playerInfo.to.clone());
    }



    /* Cinematic Yaw Methods */

    private float findClosestCinematicYaw(ObjectData data, float lastYaw) {
        double value = sensitivityX;

        double f = value * 0.6f + .2f;
        double f1 = (f * f * f) * 8f;
        return data.playerInfo.yawSmooth.smooth(lastYaw, 0.05f * (float)f1);
    }

    private float findClosestCinematicPitch(ObjectData data, float lastPitch) {
        double value = sensitivityY;

        double f = value * 0.6f + .2f;
        double f1 = (f * f * f) * 8f;
        return data.playerInfo.pitchSmooth.smooth(lastPitch, 0.05f * (float)f1);
    }

    private static int getDeltaX(double yawDelta, double gcd) {
        double f2 = yawToF2(yawDelta);

        return MathUtils.floor(f2 / getF1FromYaw(gcd));
    }

    private static int getDeltaY(double pitchDelta, double gcd) {
        double f3 = pitchToF3(pitchDelta);

        return MathUtils.floor(f3 / getF1FromPitch(gcd));
    }

    public static int sensToPercent(double sensitivity) {
        return (int) MathUtils.round(
                sensitivity / .5f * 100, 0,
                RoundingMode.HALF_UP);
    }

    //TODO Condense. This is just for easy reading until I test everything.
    private static double getSensitivityFromYawGCD(double gcd) {
        double stepOne = yawToF2(gcd) / 8;
        double stepTwo = Math.cbrt(stepOne);
        double stepThree = stepTwo - .2f;
        return stepThree / .6f;
    }

    //TODO Condense. This is just for easy reading until I test everything.
    private static double getSensitivityFromPitchGCD(double gcd) {
        double stepOne = pitchToF3(gcd) / 8;
        double stepTwo = Math.cbrt(stepOne);
        double stepThree = stepTwo - .2f;
        return stepThree / .6f;
    }

    private static double getF1FromYaw(double gcd) {
        double f = getFFromYaw(gcd);

        return Math.pow(f, 3) * 8;
    }

    private static double getFFromYaw(double gcd) {
        double sens = getSensitivityFromYawGCD(gcd);
        return sens * .6f + .2;
    }

    private static double getFFromPitch(double gcd) {
        double sens = getSensitivityFromPitchGCD(gcd);
        return sens * .6f + .2;
    }

    private static double getF1FromPitch(double gcd) {
        double f = getFFromPitch(gcd);

        return (float)Math.pow(f, 3) * 8;
    }

    private static double yawToF2(double yawDelta) {
        return yawDelta / .15;
    }

    private static double pitchToF3(double pitchDelta) {
        int b0 = pitchDelta >= 0 ? 1 : -1; //Checking for inverted mouse.
        return pitchDelta / .15 / b0;
    }

}
