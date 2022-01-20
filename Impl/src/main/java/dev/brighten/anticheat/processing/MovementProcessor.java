package dev.brighten.anticheat.processing;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.*;
import cc.funkemunky.api.utils.handlers.PlayerSizeHandler;
import cc.funkemunky.api.utils.objects.VariableValue;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import cc.funkemunky.api.utils.world.types.RayCollision;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.listeners.api.impl.KeepaliveAcceptedEvent;
import dev.brighten.anticheat.utils.AimbotUtil;
import dev.brighten.anticheat.utils.FastTrig;
import dev.brighten.anticheat.utils.MiscUtils;
import dev.brighten.anticheat.utils.MouseFilter;
import dev.brighten.anticheat.utils.MovementUtils;
import dev.brighten.anticheat.utils.api.BukkitAPI;
import dev.brighten.anticheat.utils.timer.Timer;
import dev.brighten.anticheat.utils.timer.impl.TickTimer;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.LinkedList;
import java.util.List;

public class MovementProcessor {
    private final ObjectData data;

    public LinkedList<Float> yawGcdList = new EvictingList<>(45),
            pitchGcdList = new EvictingList<>(45);
    public float deltaX, deltaY, lastDeltaX, lastDeltaY, smoothYaw, smoothPitch, lsmoothYaw, lsmoothPitch;
    public Tuple<List<Float>, List<Float>> yawOutliers, pitchOutliers;
    public float sensitivityX, sensitivityY, currentSensX, currentSensY, sensitivityMcp, yawMode, pitchMode;
    public int sensXPercent, sensYPercent;
    public TickTimer lastCinematic = new TickTimer(2);
    private MouseFilter mxaxis = new MouseFilter(), myaxis = new MouseFilter();
    private float smoothCamFilterX, smoothCamFilterY, smoothCamYaw, smoothCamPitch;
    private Timer lastReset = new TickTimer(2), generalProcess = new TickTimer(3);
    private GameMode lastGamemode;
    public boolean accurateYawData;
    public static float offset = (int)Math.pow(2, 24);
    public static double groundOffset = 1 / 64.;
    public final EvictingList<Integer> sensitivitySamples = new EvictingList<>(50);
    private static String keepaliveAcceptListener = Kauri.INSTANCE.eventHandler
            .listen(KeepaliveAcceptedEvent.class,  listner -> {
                if(listner.getData().playerInfo.serverGround || listner.getData().playerInfo.clientGround) {
                    listner.getData().playerInfo.kGroundTicks++;
                    listner.getData().playerInfo.kAirTicks = 0;
                } else {
                    listner.getData().playerInfo.kAirTicks++;
                    listner.getData().playerInfo.kGroundTicks = 0;
                }
    });

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

    public void moveTo(Location location) {
        data.playerInfo.to.x = data.playerInfo.from.x = location.getX();
        data.playerInfo.to.y = data.playerInfo.from.y = location.getY();
        data.playerInfo.to.z = data.playerInfo.from.z = location.getZ();
        data.playerInfo.to.yaw = data.playerInfo.from.yaw = location.getYaw();
        data.playerInfo.to.pitch = data.playerInfo.from.pitch = location.getPitch();

        data.playerInfo.deltaX = data.playerInfo.deltaY = data.playerInfo.deltaZ = data.playerInfo.deltaXZ
                = data.playerInfo.lDeltaX = data.playerInfo.lDeltaY = data.playerInfo.lDeltaZ
                = data.playerInfo.lDeltaXZ = 0;

        data.playerInfo.deltaYaw = data.playerInfo.lDeltaYaw =
                data.playerInfo.deltaPitch = data.playerInfo.lDeltaPitch = 0;
        data.playerInfo.moveTicks = 0;
        data.playerInfo.doingTeleport = data.playerInfo.inventoryOpen  = false;
        data.playerInfo.lastTeleportTimer.reset();
    }

    public void process(WrappedInFlyingPacket packet, long timeStamp) {
        if(data.playerInfo.checkMovement) data.playerInfo.moveTicks++;
        else {
            data.playerInfo.moveTicks = 0;
        }

        if((data.playerInfo.doingTeleport = (data.playerInfo.moveTicks == 0 || data.teleportsToConfirm > 0))) {
            data.playerInfo.lastTeleportTimer.reset();
        }
        //We check if it's null and intialize the from and to as equal to prevent large deltas causing false positives since there
        //was no previous from (Ex: delta of 380 instead of 0.45 caused by jump jump in location from 0,0,0 to 380,0,0)

        if(data.playerInfo.moveTicks > 0) {

            if (data.playerInfo.from == null && packet.isPos()) {
                data.playerInfo.from
                        = data.playerInfo.to
                        = new KLocation(packet.getX(), packet.getY(), packet.getZ(), packet.getYaw(), packet.getPitch(), timeStamp);
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
                synchronized (data.blockInfo.belowCollisions) {
                    val optional = data.blockInfo.belowCollisions.stream()
                            .filter(box -> Math.pow(box.yMax - data.playerInfo.to.y, 2) <= 9.0E-4D && data.box.copy()
                                    .offset(0, -.1, 0).isCollided(box)).findFirst();

                    if(optional.isPresent()) {
                        data.playerInfo.to.y-= data.playerInfo.to.y - optional.get().yMax;
                        data.playerInfo.clientGround = data.playerInfo.serverGround = true;
                    }
                }
            }

            if(data.playerInfo.serverGround && data.playerInfo.lastMoveCancel.isPassed()) {
                data.playerInfo.setbackLocation = new Location(data.getPlayer().getWorld(),
                        data.playerInfo.to.x, data.playerInfo.to.y, data.playerInfo.to.z,
                        data.playerInfo.to.yaw, data.playerInfo.to.pitch);
            }

            data.playerInfo.to.timeStamp = timeStamp;

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

            if(packet.isPos()) {
                //We create a separate from BoundingBox for the predictionService since it should operate on pre-motion data.
                data.box = PlayerSizeHandler.instance.bounds(data.getPlayer(),
                        data.playerInfo.to.x, data.playerInfo.to.y, data.playerInfo.to.z);
                data.playerInfo.lastMoveTimer.reset();
            }
            data.blockInfo.runCollisionCheck();

            if(packet.isPos() || packet.isLook()) {
                KLocation origin = data.playerInfo.to.clone();
                origin.y+= data.playerInfo.sneaking ? 1.54f : 1.62f;
                RayCollision collision = new RayCollision(origin.toVector(), MathUtils.getDirection(origin));

                synchronized (data.getLookingAtBoxes()) {
                    data.getLookingAtBoxes().clear();
                    data.getLookingAtBoxes().addAll(collision
                            .boxesOnRay(data.getPlayer().getWorld(),
                                    data.getPlayer().getGameMode().equals(GameMode.CREATIVE) ? 6.0 : 5.0));
                    data.playerInfo.lookingAtBlock = data.getLookingAtBoxes().size() > 0;
                }
            }
        }

        if(!data.getPlayer().getGameMode().equals(lastGamemode)) data.playerInfo.lastGamemodeTimer.reset();
        lastGamemode = data.getPlayer().getGameMode();
        data.playerInfo.creative = !data.getPlayer().getGameMode().equals(GameMode.SURVIVAL)
                && !data.getPlayer().getGameMode().equals(GameMode.ADVENTURE);

        data.blockInfo.fromFriction = data.blockInfo.currentFriction;
        if(data.playerInfo.blockBelow != null) {
            val mat = XMaterial.matchXMaterial(
                    data.playerInfo.blockBelow.getType().name());

            mat.ifPresent(xMaterial -> data.blockInfo.currentFriction = BlockUtils.getFriction(xMaterial));
        }

        if(data.playerInfo.nearGround || data.playerInfo.serverGround) data.playerInfo.nearGroundTimer.reset();

        if(data.playerInfo.calcVelocityY > 0) {
            data.playerInfo.calcVelocityY-= 0.08f;
            data.playerInfo.calcVelocityY*= 0.98f;
        } else data.playerInfo.calcVelocityY = 0;

        if(Math.abs(data.playerInfo.calcVelocityX) > 0.005) {
            data.playerInfo.calcVelocityX*= data.playerInfo.lClientGround
                    ? data.blockInfo.currentFriction * 0.91f : 0.91f;
        } else data.playerInfo.calcVelocityX = 0;

        if(Math.abs(data.playerInfo.calcVelocityZ) > 0.005) {
            data.playerInfo.calcVelocityZ*= data.playerInfo.lClientGround
                    ? data.blockInfo.currentFriction * 0.91f : 0.91f;
        } else data.playerInfo.calcVelocityZ = 0;

        //Setting player's previous locations
        if(packet.isPos() && !data.playerInfo.doingTeleport & !data.playerInfo.canFly && !data.playerInfo.creative
                && !data.playerInfo.inVehicle && timeStamp - data.creation > 500L) {

            synchronized (data.pastLocations) { //To prevent ConcurrentModificationExceptions
                data.pastLocations.add(new Tuple<>(data.playerInfo.to.clone(),
                        data.playerInfo.deltaXZ + Math.abs(data.playerInfo.deltaY)));
            }
        }


        synchronized (data.playerInfo.velocities) {
            for (Vector velocity : data.playerInfo.velocities) {
                if(Math.abs(velocity.getY() - data.playerInfo.deltaY) < 0.01) {
                    if(data.playerInfo.doingVelocity) {
                        data.playerInfo.lastVelocity.reset();

                        data.playerInfo.doingVelocity = false;
                        data.playerInfo.lastVelocityTimestamp = System.currentTimeMillis();
                        data.predictionService.velocity = true;
                        data.playerInfo.cva = data.playerInfo.cvb = data.playerInfo.cvc = true;
                        data.playerInfo.velocityX = data.playerInfo.calcVelocityX = (float) velocity.getX();
                        data.playerInfo.velocityY = data.playerInfo.calcVelocityY = (float) velocity.getY();
                        data.playerInfo.velocityZ = data.playerInfo.calcVelocityZ = (float) velocity.getZ();
                        data.playerInfo.velocityXZ = Math.hypot(data.playerInfo.velocityX, data.playerInfo.velocityZ);
                    }
                    data.playerInfo.velocities.remove(velocity);
                    break;
                }
            }

            if(data.playerInfo.insideBlock = (data.playerInfo.blockOnTo != null && !data.playerInfo.blockOnTo.getType()
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
                float deltaYaw = Math.abs(data.playerInfo.deltaYaw), lastDeltaYaw = Math.abs(data.playerInfo.lDeltaYaw);
                final double differenceYaw = Math.abs(data.playerInfo.deltaYaw - lastDeltaYaw);
                final double differencePitch = Math.abs(data.playerInfo.deltaPitch - data.playerInfo.lDeltaPitch);

                final double joltYaw = Math.abs(differenceYaw - deltaYaw);
                final double joltPitch = Math.abs(differencePitch - data.playerInfo.deltaPitch);

                final float yawThreshold = Math.max(1.0f, deltaYaw / 2f),
                        pitchThreshold = Math.max(1.f, Math.abs(data.playerInfo.deltaPitch) / 2f);

                if (joltYaw > yawThreshold && joltPitch > pitchThreshold) data.playerInfo.lastHighRate.reset();
                data.playerInfo.lastPitchGCD = data.playerInfo.pitchGCD;
                data.playerInfo.lastYawGCD = data.playerInfo.yawGCD;
                data.playerInfo.yawGCD = MiscUtils
                        .gcdSmall(data.playerInfo.deltaYaw, data.playerInfo.lDeltaYaw);
                data.playerInfo.pitchGCD = MiscUtils
                        .gcdSmall(data.playerInfo.deltaPitch, data.playerInfo.lDeltaPitch);

                val origin = data.playerInfo.to.clone();

                origin.y+= data.playerInfo.sneaking ? 1.54 : 1.62;

                if(data.playerInfo.lastTeleportTimer.isPassed(1)) {
                    predictionHandling:
                    {
                        float yawGcd = data.playerInfo.yawGCD,
                                pitchGcd = data.playerInfo.pitchGCD;

                        //Adding gcd of yaw and pitch.
                        if (data.playerInfo.yawGCD > 0.01 && data.playerInfo.yawGCD < 1.2) {
                            yawGcdList.add(yawGcd);
                        }
                        if (data.playerInfo.pitchGCD > 0.01 && data.playerInfo.pitchGCD < 1.2)
                            pitchGcdList.add(pitchGcd);

                        if(yawGcdList.size() < 20 || pitchGcdList.size() < 20) {
                            accurateYawData = false;
                            break predictionHandling;
                        }

                        accurateYawData = true;

                        //Making sure to get shit within the std for a more accurate result.

                        //Making sure to get shit within the std for a more accurate result.
                        currentSensX = getSensitivityFromYawGCD(yawGcd);
                        currentSensY = getSensitivityFromPitchGCD(pitchGcd);
                        if (lastReset.isPassed()) {
                            yawOutliers = MiscUtils.getOutliersFloat(yawGcdList);
                            pitchOutliers = MiscUtils.getOutliersFloat(pitchGcdList);
                            yawMode = MathUtils.getMode(yawGcdList);
                            pitchMode = MathUtils.getMode(pitchGcdList);
                            lastReset.reset();
                            sensXPercent = sensToPercent(sensitivityX = getSensitivityFromYawGCD(yawMode));
                            sensYPercent = sensToPercent(sensitivityY = getSensitivityFromPitchGCD(pitchMode));

                            table: {
                                sensitivitySamples.add(Math.max(sensXPercent, sensYPercent));

                                if (sensitivitySamples.size() > 30) {
                                    final long mode = MathUtils.getMode(sensitivitySamples);

                                    sensitivityMcp = AimbotUtil.SENSITIVITY_MAP.getOrDefault((int) mode, -1.0F);
                                }
                            }
                        }


                        lastDeltaX = deltaX;
                        lastDeltaY = deltaY;
                        deltaX = getExpiermentalDeltaX(data);
                        deltaY = getExpiermentalDeltaY(data);

                        if ((data.playerInfo.pitchGCD < 0.006 && data.playerInfo.yawGCD < 0.006) && smoothCamFilterY < 1E6
                                && smoothCamFilterX < 1E6 && timeStamp - data.creation > 1000L) {
                            float sens = MovementProcessor.percentToSens(95);
                            float f = sens * 0.6f + .2f;
                            float f1 = f * f * f * 8;
                            float f2 = deltaX * f1;
                            float f3 = deltaY * f1;

                            smoothCamFilterX = mxaxis.smooth(smoothCamYaw, .05f * f1);
                            smoothCamFilterY = myaxis.smooth(smoothCamPitch, .05f * f1);

                            this.smoothCamYaw += f2;
                            this.smoothCamPitch += f3;

                            f2 = smoothCamFilterX * 0.5f;
                            f3 = smoothCamFilterY * 0.5f;

                            //val clampedFrom = (Math.abs(data.playerInfo.from.yaw) > 360 ? data.playerInfo.from.yaw % 360 : data.playerInfo.from.yaw);
                            val clampedFrom = MathUtils.yawTo180F(data.playerInfo.from.yaw);
                            float pyaw = clampedFrom + f2 * .15f;
                            float ppitch = data.playerInfo.from.pitch - f3 * .15f;

                            this.lsmoothYaw = smoothYaw;
                            this.lsmoothPitch = smoothPitch;
                            this.smoothYaw = pyaw;
                            this.smoothPitch = ppitch;

                            float yaccel = Math.abs(data.playerInfo.deltaYaw) - Math.abs(data.playerInfo.lDeltaYaw),
                                    pAccel = Math.abs(data.playerInfo.deltaPitch) - Math.abs(data.playerInfo.lDeltaPitch);

                            if (MathUtils.getDelta(smoothYaw, clampedFrom) > (yaccel > 0 ? (yaccel > 10 ? 2.5 : 1) : 0.3)
                                    || MathUtils.getDelta(smoothPitch, data.playerInfo.from.pitch)
                                    > (pAccel > 0 ? (pAccel > 10 ? 2.5 : 1) : 0.3)) {
                                smoothCamYaw = smoothCamPitch = 0;
                                data.playerInfo.cinematicMode = false;
                                mxaxis.reset();
                                myaxis.reset();
                            } else data.playerInfo.cinematicMode = true;

                            MiscUtils.testMessage("syaw=" + smoothYaw + " spitch=" + smoothPitch + " yaw=" + MathUtils.yawTo180F(data.playerInfo.from.yaw) + " pitch=" + data.playerInfo.from.pitch);
                        } else {
                            mxaxis.reset();
                            myaxis.reset();
                            data.playerInfo.cinematicMode = false;
                        }

                        lastDeltaX = deltaX;
                        lastDeltaY = deltaY;
                        deltaX = getExpiermentalDeltaX(data);
                        deltaY = getExpiermentalDeltaY(data);
                    }
                } else {
                    yawGcdList.clear();
                    pitchGcdList.clear();
                }
            }
            //Running jump check
            if (!data.playerInfo.clientGround && !data.playerInfo.doingTeleport) {
                if (!data.playerInfo.jumped && data.playerInfo.lClientGround
                        && data.playerInfo.deltaY >= 0) {
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

            //Checking if the player was collided with ghost blocks.
            synchronized (data.ghostBlocks) {
                SimpleCollisionBox boxToCheck = data.box.copy().expand(0.4f);
                for (Location location : data.ghostBlocks.keySet()) {
                    if(location.toVector().distanceSquared(data.playerInfo.to.toVector()) > 25) continue;

                    if(data.ghostBlocks.get(location).isCollided(boxToCheck)) {
                        data.playerInfo.lastGhostCollision.reset();
                        break;
                    }
                }
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
            if (data.blockInfo.collidedWithEntity) data.playerInfo.lastEntityCollision.reset();

            //Player ground/air positioning ticks.
            if (!data.playerInfo.clientGround) {
                data.playerInfo.airTicks++;
                data.playerInfo.groundTicks = 0;
            } else {
                data.playerInfo.groundTicks++;
                data.playerInfo.airTicks = 0;
            }

            data.playerInfo.baseSpeed = MovementUtils.getBaseSpeed(data);
        }

        if(data.playerInfo.inVehicle = data.getPlayer().getVehicle() != null) {
            data.playerInfo.vehicleTimer.reset();
            data.runKeepaliveAction(ka -> data.playerInfo.vehicleTimer.reset());
        }
        if(data.playerInfo.gliding = BukkitAPI.INSTANCE.isGliding(data.getPlayer()))
            data.playerInfo.lastGlideTimer.reset();
        data.playerInfo.riptiding = Atlas.getInstance().getBlockBoxManager()
                .getBlockBox().isRiptiding(data.getPlayer());
        /* We only set the jumpheight on ground since there's no need to check for it while they're in the air.
         * If we did check while it was in the air, there would be false positives in the checks that use it. */
        if (packet.isGround() || data.playerInfo.serverGround || data.playerInfo.lClientGround) {
            data.playerInfo.jumpHeight = MovementUtils.getJumpHeight(data);
            data.playerInfo.totalHeight = MovementUtils.getTotalHeight(data.playerVersion,
                    (float)data.playerInfo.jumpHeight);
        }

        if(data.blockInfo.fenceBelow)
            data.playerInfo.lastFenceBelow.reset();

        if(!data.playerInfo.worldLoaded)
            data.playerInfo.lastChunkUnloaded.reset();

        data.lagInfo.lagging = data.lagInfo.lagTicks.subtract() > 0
                || !data.playerInfo.worldLoaded
                || timeStamp - Kauri.INSTANCE.lastTick >
                new VariableValue<>(110, 60, ProtocolVersion::isPaper).get();

        if (packet.isPos()) {
            if (data.playerInfo.serverGround && data.playerInfo.groundTicks > 4)
                data.playerInfo.groundLoc = data.playerInfo.to;
        }

        if (data.playerInfo.canFly != data.getPlayer().getAllowFlight()) {
            data.playerInfo.lastToggleFlight.reset();
        }
        data.playerInfo.canFly = data.getPlayer().getAllowFlight();

        if (data.playerInfo.breakingBlock) data.playerInfo.lastBrokenBlock.reset();

        //Setting fallDistance
        if (!data.playerInfo.serverGround
                && data.playerInfo.deltaY < 0
                && !data.playerInfo.doingTeleport
                && !data.blockInfo.onClimbable
                && !data.blockInfo.inLiquid
                && !data.blockInfo.inWeb) {
            data.playerInfo.fallDistance += -data.playerInfo.deltaY;
        } else data.playerInfo.fallDistance = 0;

        /* General Cancel Booleans */
        boolean hasLevi = levitation != null && data.potionProcessor.hasPotionEffect(levitation);

        data.playerInfo.generalCancel = data.getPlayer().getAllowFlight()
                || data.playerInfo.creative
                || hasLevi
                || data.getPlayer().isSleeping()
                || data.playerInfo.lastGhostCollision.isNotPassed()
                || data.playerInfo.doingTeleport
                || data.playerInfo.lastTeleportTimer.isNotPassed(1)
                || data.playerInfo.riptiding
                || data.playerInfo.gliding
                || data.playerInfo.lastPlaceLiquid.isNotPassed(5)
                || data.playerInfo.inVehicle
                || (data.playerInfo.lastChunkUnloaded.isNotPassed(35)
                && MathUtils.getDelta(-0.098, data.playerInfo.deltaY) < 0.0001)
                || timeStamp - data.playerInfo.lastRespawn < 2500L
                || data.playerInfo.lastToggleFlight.isNotPassed(40)
                || timeStamp - data.creation < 4000
                || Kauri.INSTANCE.lastTickLag.isNotPassed(5);

        data.playerInfo.flightCancel = data.playerInfo.generalCancel
                || data.playerInfo.webTimer.isNotPassed(8)
                || data.playerInfo.liquidTimer.isNotPassed(8)
                || data.playerInfo.onLadder
                || data.blockInfo.inScaffolding
                || data.blockInfo.inHoney
                || !data.playerInfo.checkMovement
                || data.playerInfo.lastTeleportTimer.isNotPassed(1)
                || data.playerInfo.lastGlideTimer.isNotPassed()
                || (data.playerInfo.deltaXZ == 0 && data.playerInfo.deltaY == 0)
                || data.blockInfo.roseBush
                || data.playerInfo.doingVelocity
                || data.playerInfo.lastVelocity.isNotPassed(3)
                || data.playerInfo.slimeTimer.isNotPassed(8)
                || data.playerInfo.climbTimer.isNotPassed(6)
                || data.playerInfo.lastHalfBlock.isNotPassed(5);
    }
    private static float getDeltaX(float yawDelta, float gcd) {
        return MathHelper.ceiling_float_int(yawDelta / gcd);
    }

    private static float getDeltaY(float pitchDelta, float gcd) {
        return MathHelper.ceiling_float_int(pitchDelta / gcd);
    }

    public static float getExpiermentalDeltaX(ObjectData data) {
        float deltaPitch = data.playerInfo.deltaYaw;
        float sens = data.moveProcessor.sensitivityX;
        float f = sens * 0.6f + .2f;
        float calc = f * f * f * 8;

        float result = deltaPitch / (calc * .15f);

        return result;
    }

    public static float getExpiermentalDeltaY(ObjectData data) {
        float deltaPitch = data.playerInfo.deltaPitch;
        float sens = data.moveProcessor.sensitivityY;
        float f = sens * 0.6f + .2f;
        float calc = f * f * f * 8;

        float result = deltaPitch / (calc * .15f);

        return result;
    }

    public static int sensToPercent(float sensitivity) {
        return MathHelper.floor_float(sensitivity / .5f * 100);
    }

    public static float percentToSens(int percent) {
        return percent * .0070422534f;
    }

    public static float getSensitivityFromYawGCD(float gcd) {
        return ((float) Math.cbrt(yawToF2(gcd) / 8f) - .2f) / .6f;
    }

    private static float getSensitivityFromPitchGCD(float gcd) {
        return ((float)Math.cbrt(pitchToF3(gcd) / 8f) - .2f) / .6f;
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
        return yawDelta / .15f;
    }

    private static float pitchToF3(float pitchDelta) {
        int b0 = pitchDelta >= 0 ? 1 : -1; //Checking for inverted mouse.
        return (pitchDelta / b0) / .15f;
    }

}
