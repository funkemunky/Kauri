package dev.brighten.anticheat.processing;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.BoundingBox;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.utils.KLocation;
import dev.brighten.anticheat.utils.MovementUtils;
import dev.brighten.anticheat.utils.VanillaUtils;
import org.bukkit.Bukkit;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Optional;

public class MovementProcessor {

    public static void preProcess(ObjectData data, WrappedInFlyingPacket packet) {
        /* Pre Motion Y Prediction */
        //Thing in Minecraft that prevents really large numbers.
        data.playerInfo.lpDeltaY = data.playerInfo.pDeltaY;
        if(Math.abs(data.playerInfo.pDeltaY) < 0.005) {
            data.playerInfo.pDeltaY = 0;
        }
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

        //Setting the angle delta for use in checks to prevent repeated functions.
        data.playerInfo.lDeltaYaw = data.playerInfo.deltaYaw;
        data.playerInfo.lDeltaPitch = data.playerInfo.deltaPitch;
        data.playerInfo.deltaYaw = MathUtils.getDelta(
                MathUtils.yawTo180F(data.playerInfo.to.yaw),
                MathUtils.yawTo180F(data.playerInfo.from.yaw));
        data.playerInfo.deltaPitch = data.playerInfo.to.pitch - data.playerInfo.from.pitch;

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
            if(!data.playerInfo.jumped && !data.playerInfo.inAir) {
               data.playerInfo.jumped = true;
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
    }
}
