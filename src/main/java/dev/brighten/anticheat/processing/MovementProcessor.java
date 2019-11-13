package dev.brighten.anticheat.processing;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.BlockUtils;
import cc.funkemunky.api.utils.BoundingBox;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.utils.KLocation;
import dev.brighten.anticheat.utils.MiscUtils;
import dev.brighten.anticheat.utils.MovementUtils;
import org.bukkit.block.Block;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class MovementProcessor {

    private static List<Float> sensitivities = Arrays.asList(.0f, .1f, .25f, .35f, .5f, .62f, .75f, 0.9f, 1f);

    public static float offset = 16777216L;

    public static void process(ObjectData data, WrappedInFlyingPacket packet, long timeStamp) {
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

        if(data.playerInfo.testFly != data.playerInfo.lastTestFly)
            data.playerInfo.lastToggleFlight.reset();

        data.playerInfo.lastTestFly = data.playerInfo.testFly;
        data.playerInfo.testFly = data.getPlayer().getAllowFlight();

        data.playerInfo.jumpHeight = MovementUtils.getJumpHeight(data.getPlayer());

        data.playerInfo.worldLoaded = Atlas.getInstance().getBlockBoxManager().getBlockBox()
                .isChunkLoaded(data.playerInfo.to.toLocation(data.getPlayer().getWorld()));

        data.lagInfo.lagging = data.lagInfo.lastPacketDrop.hasNotPassed(3)
                || !data.playerInfo.worldLoaded
                || Kauri.INSTANCE.lastTickLag.hasNotPassed(20);

        //We set the yaw and pitch like this to prevent inaccurate data input. Like above, it will return both pitch
        //and yaw as 0 if it isnt a look packet.
        if (packet.isLook()) {
            data.playerInfo.to.yaw = packet.getYaw();
            data.playerInfo.to.pitch = packet.getPitch();
        }

        //Fixes glitch when logging in.
        if(timeStamp - data.creation < 1000) {
            data.playerInfo.serverCanFly = data.getPlayer().getAllowFlight();
            data.playerInfo.serverIsFlying = data.getPlayer().isFlying();
        }

        if (data.playerInfo.breakingBlock) {
            data.playerInfo.lastBrokenBlock.reset();
        }

        data.playerInfo.to.timeStamp = timeStamp;

        data.playerInfo.lClientGround = data.playerInfo.clientGround;
        data.playerInfo.clientGround = packet.isGround();

        //Setting boundingBox
        data.predictionService.box = new BoundingBox(data.playerInfo.from.toVector(), data.playerInfo.from.toVector())
                .grow(0.3f, 0, 0.3f)
                .add(0, 0, 0, 0, 1.8f, 0);;
        data.box = new BoundingBox(data.playerInfo.to.toVector(), data.playerInfo.to.toVector())
                .grow(0.3f, 0, 0.3f)
                .add(0, 0, 0, 0, 1.8f, 0);

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

            data.playerInfo.cinematicModeYaw = MathUtils.getDelta(data.playerInfo.cDeltaYaw, data.playerInfo.deltaYaw) < (Math.abs(data.playerInfo.deltaYaw) > 20 ? 2 : 0.51);

            data.playerInfo.cinematicModePitch =
                    MathUtils.getDelta(data.playerInfo.cDeltaPitch, data.playerInfo.deltaPitch)
                            < (data.playerInfo.deltaPitch > 12 ? 1.1 : (data.playerInfo.deltaYaw > 15 ? 0.55f : 0.31)) && Math.abs(data.playerInfo.deltaPitch) > 1E-7;

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

        Block block = data.playerInfo.worldLoaded ? data.playerInfo.to.toLocation(data.getPlayer().getWorld()).getBlock() : null;

        if (block != null && BlockUtils.isClimbableBlock(block)) {
            if (data.playerInfo.collidesHorizontally) {
                data.blockInfo.onClimbable = true;
            } else data.blockInfo.onClimbable = data.playerInfo.deltaY <= 0;
            data.playerInfo.onLadder = true;
        } else data.playerInfo.onLadder = data.blockInfo.onClimbable = false;

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

        data.playerInfo.flightCancel = data.playerInfo.serverCanFly
                || data.playerInfo.inCreative
                || hasLevi
                || data.playerInfo.webTicks > 0
                || !data.playerInfo.worldLoaded
                || block == null
                || data.playerInfo.lastToggleFlight.hasNotPassed(40)
                || data.playerInfo.liquidTicks > 0
                || data.playerInfo.climbTicks > 0
                || timeStamp - data.creation < 2000
                || data.playerInfo.serverPos
                || Kauri.INSTANCE.lastTickLag.hasNotPassed(5);

        data.playerInfo.generalCancel = data.playerInfo.serverCanFly
                || data.playerInfo.inCreative
                || hasLevi
                || !data.playerInfo.worldLoaded
                || data.playerInfo.lastToggleFlight.hasNotPassed(40)
                || timeStamp - data.creation < 2000
                || block == null
                || data.playerInfo.serverPos
                || Kauri.INSTANCE.lastTickLag.hasNotPassed(5);

        //Adding past location
        data.pastLocation.addLocation(data.playerInfo.to.clone());
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
