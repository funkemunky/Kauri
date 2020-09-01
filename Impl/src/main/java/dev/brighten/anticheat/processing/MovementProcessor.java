package dev.brighten.anticheat.processing;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.reflections.impl.MinecraftReflection;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.types.MathHelper;
import cc.funkemunky.api.utils.*;
import cc.funkemunky.api.utils.handlers.PlayerSizeHandler;
import cc.funkemunky.api.utils.objects.VariableValue;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.utils.MiscUtils;
import dev.brighten.anticheat.utils.MouseFilter;
import dev.brighten.anticheat.utils.MovementUtils;
import dev.brighten.anticheat.utils.TickTimer;
import lombok.val;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.potion.PotionEffectType;

import java.math.RoundingMode;
import java.util.Deque;
import java.util.List;

public class MovementProcessor {
    private final ObjectData data;

    public Deque<Float> yawGcdList = new EvictingList<>(50),
            pitchGcdList = new EvictingList<>(50);
    public float deltaX, deltaY, lastDeltaX, lastDeltaY;
    public Tuple<List<Double>, List<Double>> yawOutliers, pitchOutliers;
    public long lastCinematic;
    public float sensitivityX, sensitivityY, yawMode, pitchMode, sensXPercent, sensYPercent;
    private MouseFilter mxaxis = new MouseFilter(), myaxis = new MouseFilter();
    private float smoothCamFilterX, smoothCamFilterY, smoothCamYaw, smoothCamPitch;
    private TickTimer lastReset = new TickTimer(1), generalProcess = new TickTimer(3);
    private GameMode lastGamemode;
    public static float offset = (int)Math.pow(2, 24);

    public PotionEffectType levitation = null;

    public MovementProcessor(ObjectData data) {
        this.data = data;

        if(ProtocolVersion.getGameVersion().isOrAbove(ProtocolVersion.V1_8)) {
            try {
                levitation = PotionEffectType.getByName("LEVITATION");
            } catch(Exception e) {

            }
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
            //if this is the case, this assumes client movement in between therefore we have to calculate where ground would be.
        } else if(packet.isGround() && !data.playerInfo.clientGround) { //this is the last ground
            val optional = data.blockInfo.belowCollisions.stream()
                    .filter(box -> Math.pow(box.yMax - data.playerInfo.to.y, 2) <= 9.0E-4D && data.box.copy()
                            .offset(0, -.1, 0).isCollided(box)).findFirst();

            if(optional.isPresent()) {
                data.playerInfo.to.y-= data.playerInfo.to.y - optional.get().yMax;
                data.playerInfo.clientGround = data.playerInfo.serverGround = true;
            }
        }

        data.playerInfo.to.timeStamp = timeStamp;
        //Adding past location
        data.pastLocation.addLocation(data.playerInfo.to);

        if (data.playerInfo.posLocs.size() > 0 && !packet.isGround()) {
            val optional = data.playerInfo.posLocs.stream()
                    .filter(loc -> loc.x == packet.getX() && loc.y == packet.getY() && loc.z == packet.getZ())
                    .findFirst();

            if (optional.isPresent()) {
                data.playerInfo.serverPos = true;
                data.playerInfo.lastServerPos = timeStamp;
                data.playerInfo.lastTeleportTimer.reset();
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
        data.playerInfo.deltaX = data.playerInfo.serverPos ? 0 : data.playerInfo.to.x - data.playerInfo.from.x;
        data.playerInfo.deltaY = data.playerInfo.serverPos ? 0 : data.playerInfo.to.y - data.playerInfo.from.y;
        data.playerInfo.deltaZ = data.playerInfo.serverPos ? 0 : data.playerInfo.to.z - data.playerInfo.from.z;
        data.playerInfo.lDeltaXZ = data.playerInfo.deltaXZ;
        data.playerInfo.deltaXZ = data.playerInfo.serverPos ? 0 : MathUtils.hypot(data.playerInfo.deltaX, data.playerInfo.deltaZ);

        data.playerInfo.blockOnTo = BlockUtils.getBlock(data.playerInfo.to.toLocation(data.getPlayer().getWorld()));
        data.playerInfo.blockBelow = BlockUtils.getBlock(data.playerInfo.to.toLocation(data.getPlayer().getWorld())
                .subtract(0, 1, 0));

        if(!data.getPlayer().getGameMode().equals(lastGamemode)) data.playerInfo.lastGamemodeTimer.reset();
        lastGamemode = data.getPlayer().getGameMode();
        data.playerInfo.creative = !data.getPlayer().getGameMode().equals(GameMode.SURVIVAL)
                && !data.getPlayer().getGameMode().equals(GameMode.ADVENTURE);

        if(data.playerInfo.blockBelow != null)
            data.blockInfo.currentFriction = MinecraftReflection.getFriction(data.playerInfo.blockBelow);

        Block block = BlockUtils.getBlock(new Location(data.getPlayer().getWorld(),
                data.playerInfo.from.x, data.playerInfo.from.y - 1, data.playerInfo.from.z));

        if(block != null)
            data.blockInfo.fromFriction = MinecraftReflection.getFriction(block);

        if(packet.isPos()) {
            //We create a separate from BoundingBox for the predictionService since it should operate on pre-motion data.
            data.box = PlayerSizeHandler.instance.bounds(data.getPlayer(),
                    data.playerInfo.to.x, data.playerInfo.to.y, data.playerInfo.to.z);

            if(timeStamp - data.creation > 400L) data.blockInfo.runCollisionCheck(); //run b4 everything else for use below.
        }

        if(MathUtils.getDelta(deltaY, -0.098) < 0.001) {
            data.playerInfo.worldLoaded = false;
        }
        data.playerInfo.inVehicle = data.getPlayer().getVehicle() != null;
        data.playerInfo.gliding = PlayerUtils.isGliding(data.getPlayer());
        data.playerInfo.riptiding = Atlas.getInstance().getBlockBoxManager()
                .getBlockBox().isRiptiding(data.getPlayer());
        /* We only set the jumpheight on ground since there's no need to check for it while they're in the air.
         * If we did check while it was in the air, there would be false positives in the checks that use it. */
        if (packet.isGround() || data.playerInfo.serverGround || data.playerInfo.lClientGround) {
            data.playerInfo.jumpHeight = MovementUtils.getJumpHeight(data);
            data.playerInfo.totalHeight = MovementUtils.getTotalHeight(data.playerVersion,
                    (float)data.playerInfo.jumpHeight);
        }

        if(Atlas.getInstance().getBlockBoxManager().getBlockBox()
                .isChunkLoaded(data.playerInfo.to.toLocation(data.getPlayer().getWorld())))
            data.playerInfo.lastChunkUnloaded.reset();

        data.playerInfo.lworldLoaded = data.playerInfo.worldLoaded;

        if(MathUtils.getDelta(data.playerInfo.deltaY, -0.098) < 0.0001
                && data.playerInfo.lastChunkUnloaded.hasNotPassed(35))
            data.playerInfo.worldLoaded = false;
        else data.playerInfo.worldLoaded = true;


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
        if(packet.isLook()) {
            data.playerInfo.to.yaw = packet.getYaw();
            data.playerInfo.to.pitch = packet.getPitch();
        }

        //Setting the angle delta for use in checks to prevent repeated functions.
        data.playerInfo.lDeltaYaw = data.playerInfo.deltaYaw;
        data.playerInfo.lDeltaPitch = data.playerInfo.deltaPitch;
        data.playerInfo.deltaYaw = data.playerInfo.to.yaw
                - data.playerInfo.from.yaw;
        data.playerInfo.deltaPitch = data.playerInfo.to.pitch - data.playerInfo.from.pitch;
        if (packet.isLook()) {

            data.playerInfo.lastPitchGCD = data.playerInfo.pitchGCD;
            data.playerInfo.lastYawGCD = data.playerInfo.yawGCD;
            data.playerInfo.yawGCD = MiscUtils.gcd((int) (data.playerInfo.deltaYaw * offset),
                    (int) (data.playerInfo.lDeltaYaw * offset));
            data.playerInfo.pitchGCD = MiscUtils.gcd((int) (data.playerInfo.deltaPitch * offset),
                    (int) (data.playerInfo.lDeltaPitch * offset));

            val origin = data.playerInfo.to.clone();

            origin.y+= data.playerInfo.sneaking ? 1.54 : 1.62;

            float yawGcd = data.playerInfo.yawGCD / offset, pitchGcd = data.playerInfo.pitchGCD / offset;

            //Adding gcd of yaw and pitch.
            if (data.playerInfo.yawGCD > 160000 && data.playerInfo.yawGCD < 10500000)
                yawGcdList.add(yawGcd);
            if (data.playerInfo.pitchGCD > 160000 && data.playerInfo.pitchGCD < 10500000)
                pitchGcdList.add(pitchGcd);

            if (yawGcdList.size() > 3 && pitchGcdList.size() > 3) {

                //Making sure to get shit within the std for a more accurate result.
                if (lastReset.hasPassed()) {
                    yawMode = MathUtils.getMode(yawGcdList);
                    pitchMode = MathUtils.getMode(pitchGcdList);
                    yawOutliers = MiscUtils.getOutliers(yawGcdList);
                    pitchOutliers = MiscUtils.getOutliers(pitchGcdList);
                    lastReset.reset();
                    sensXPercent = sensToPercent(sensitivityX = getSensitivityFromYawGCD(yawMode));
                    sensYPercent = sensToPercent(sensitivityY = getSensitivityFromPitchGCD(pitchMode));
                }


                lastDeltaX = deltaX;
                lastDeltaY = deltaY;
                deltaX = getDeltaX(data.playerInfo.deltaYaw, yawMode);
                deltaY = getDeltaY(data.playerInfo.deltaPitch, pitchMode);

                if((data.playerInfo.pitchGCD < 1E5 || data.playerInfo.yawGCD < 1E5) && smoothCamFilterY < 1E6
                        && smoothCamFilterX < 1E6 && timeStamp - data.creation > 1000L) {
                    float f = sensitivityX * 0.6f + .2f;
                    float f1 = f * f * f * 8;
                    float f2 = deltaX * f1;
                    float f3 = deltaY * f1;

                    smoothCamFilterX = mxaxis.smooth(smoothCamYaw, .05f * f1);
                    smoothCamFilterY = myaxis.smooth(smoothCamPitch, .05f * f1);
                    smoothCamYaw = f2;
                    smoothCamPitch = f3;
                    f2 = smoothCamFilterX * 0.5f;
                    f3 = smoothCamFilterY * 0.5f;

                    float pyaw = data.playerInfo.from.yaw + f2 * .15f;
                    float ppitch = data.playerInfo.from.pitch - f3 * .15f;

                    if(data.playerInfo.cinematicMode =
                            (MathUtils.getDelta(pyaw, data.playerInfo.from.yaw) < (Math.abs(deltaX) > 50 ? 3 : 1)
                            && MathUtils.getDelta(ppitch, data.playerInfo.to.pitch) < (Math.abs(deltaY) > 30 ? 2 : 1))) {
                        lastCinematic = timeStamp;
                        data.playerInfo.cinematicTimer.reset();
                    }

                    //MiscUtils.testMessage("pyaw=" + pyaw + " ppitch=" + ppitch + " yaw=" + data.playerInfo.to.yaw + " pitch=" + data.playerInfo.to.pitch);
                } else {
                    mxaxis.reset();
                    myaxis.reset();
                    data.playerInfo.cinematicMode = false;
                }
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
        }

        data.playerInfo.serverAllowedFlight = data.getPlayer().getAllowFlight();
        if (data.playerInfo.breakingBlock) data.playerInfo.lastBrokenBlock.reset();

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
        boolean hasLevi = levitation != null && data.potionProcessor.hasPotionEffect(levitation);

        data.playerInfo.generalCancel = data.getPlayer().getAllowFlight()
                || data.playerInfo.creative
                || hasLevi
                || (MathUtils.getDelta(-0.098, data.playerInfo.deltaY) < 0.001
                && data.playerInfo.lastChunkUnloaded.hasNotPassed(60))
                || data.playerInfo.serverPos
                || data.playerInfo.riptiding
                || data.playerInfo.lastTeleportTimer.hasNotPassed(1)
                || data.playerInfo.gliding
                || data.playerInfo.lastPlaceLiquid.hasNotPassed(5)
                || data.playerInfo.inVehicle
                || !data.playerInfo.worldLoaded
                || timeStamp - data.playerInfo.lastRespawn < 2500L
                || data.playerInfo.lastToggleFlight.hasNotPassed(40)
                || timeStamp - data.creation < 4000
                || Kauri.INSTANCE.lastTickLag.hasNotPassed(5);

        data.playerInfo.flightCancel = data.playerInfo.generalCancel
                || data.playerInfo.webTimer.hasNotPassed(4)
                || data.playerInfo.liquidTimer.hasNotPassed(3)
                || data.playerInfo.onLadder
                || data.playerInfo.slimeTimer.hasNotPassed(5)
                || data.playerInfo.climbTimer.hasNotPassed(4)
                || data.playerInfo.lastHalfBlock.hasNotPassed(3);
    }
    private static float getDeltaX(float yawDelta, float gcd) {
        return MathHelper.floor(yawDelta / gcd);
    }

    private static float getDeltaY(float pitchDelta, float gcd) {
        return MathHelper.floor(pitchDelta / gcd);
    }

    public static int sensToPercent(float sensitivity) {
        return (int) MathUtils.round(
                sensitivity / .5f * 100, 0,
                RoundingMode.HALF_UP);
    }

    //Noncondensed
    /*private static double getSensitivityFromYawGCD(double gcd) {
        double stepOne = yawToF2(gcd) / 8;
        double stepTwo = Math.cbrt(stepOne);
        double stepThree = stepTwo - .2f;
        return stepThree / .6f;
    }*/

    //Condensed
    public static float getSensitivityFromYawGCD(float gcd) {
        return ((float)Math.cbrt((double)yawToF2(gcd) / 8) - .2f) / .6f;
    }

    //Noncondensed
    /*private static double getSensitivityFromPitchGCD(double gcd) {
        double stepOne = pitchToF3(gcd) / 8;
        double stepTwo = Math.cbrt(stepOne);
        double stepThree = stepTwo - .2f;
        return stepThree / .6f;
    }*/

    //Condensed
    private static float getSensitivityFromPitchGCD(float gcd) {
        return ((float)Math.cbrt((double)pitchToF3(gcd) / 8) - .2f) / .6f;
    }

    private static float getF1FromYaw(float gcd) {
        float f = getFFromYaw(gcd);

        return f * f * f * 8;
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
        return (float)((double)yawDelta / .15);
    }

    private static float pitchToF3(float pitchDelta) {
        int b0 = pitchDelta >= 0 ? 1 : -1; //Checking for inverted mouse.
        return (float)((double)(pitchDelta / b0) / .15);
    }

}
