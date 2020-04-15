package dev.brighten.anticheat.check.impl.movement.fly;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.world.BlockData;
import cc.funkemunky.api.utils.world.CollisionBox;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.Helper;
import dev.brighten.anticheat.utils.MovementUtils;
import dev.brighten.api.check.CheckType;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

@CheckInfo(name = "Fly (B)", description = "Checks for improper acceleration.", checkType = CheckType.FLIGHT,
        vlToFlag = 4, punishVL = 12)
@Cancellable
public class FlyB extends Check {

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        if(packet.isPos() && (data.playerInfo.deltaY != 0 || data.playerInfo.deltaXZ != 0)) {
            //We check if the player is in ground, since theoretically the y should be zero.
            double predicted = (data.playerInfo.lDeltaY - 0.08) * 0.9800000190734863D;

            if(data.playerInfo.lClientGround && !data.playerInfo.clientGround) {
                predicted = Math.min(data.playerInfo.deltaY, MovementUtils.getJumpHeight(packet.getPlayer()));
            }

            for (Block block : Helper.blockCollisions(new ArrayList<>(data.blockInfo.handler.getBlocks()),
                    data.box.copy().expand(0.25 + Math.abs(data.playerInfo.deltaX),0.5,
                            0.25 + Math.abs(data.playerInfo.deltaZ))
                            .expandMin(0, -0.5f + Math.min(0, data.playerInfo.deltaY), 0))) {
                CollisionBox box = BlockData.getData(block.getType())
                        .getBox(block, ProtocolVersion.getGameVersion());

                List<SimpleCollisionBox> sBoxes = new ArrayList<>();
                box.downCast(sBoxes);

                for (SimpleCollisionBox sBox : sBoxes) {
                    double minDelta = sBox.yMax - data.playerInfo.from.y, maxDelta = sBox.yMin - (data.playerInfo.from.y + 1.8);

                    if(MathUtils.getDelta(data.playerInfo.deltaY, minDelta) < 1E-7) {
                        predicted = minDelta;
                        break;
                    } else if(MathUtils.getDelta(data.playerInfo.deltaY, maxDelta) < 1E-7) {
                        predicted = maxDelta;
                        break;
                    }
                }
            }

            //Basically, this bug would only occur if the client's movement is less than a certain amount.
            //If it is, it won't send any position packet. Usually this only occurs when the magnitude
            //of motionY is less than 0.005 and it rounds it to 0.
            //The easiest way I found to produce this oddity is by putting myself in a corner and just jumping.
            if(Math.abs(data.playerInfo.deltaY - -0.078)  < 0.01
                    && MathUtils.getDelta(data.playerInfo.deltaY, predicted) > 0.0001
                    && Math.abs(data.playerInfo.deltaY - data.playerInfo.lDeltaY) > 0.11) {
                predicted = -0.08;
                predicted*= 0.9800000190734863D;
            }

            if((data.playerInfo.clientGround && predicted > -0.08 && predicted < -0.077)
                    || (Math.abs(predicted) < 0.005 && data.playerVersion.isOrBelow(ProtocolVersion.V1_8_9))) {
                predicted = 0;
            }

            double deltaPredict =  MathUtils.getDelta(data.playerInfo.deltaY, predicted);

            if(!data.playerInfo.flightCancel
                    && (MathUtils.getDelta(-0.098, data.playerInfo.deltaY) > 0.001 || data.playerInfo.deltaXZ > 0.3)
                    && !(data.playerInfo.blockOnTo != null && data.playerInfo.blockOnTo.getType().isSolid())
                    && (!data.blockInfo.blocksAbove || data.playerInfo.deltaY >= 0)
                    && data.playerInfo.slimeTimer.hasPassed(20)
                    && timeStamp - data.playerInfo.lastServerPos > 100L
                    && data.playerInfo.liquidTimer.hasPassed(8)
                    && data.playerInfo.lastBlockPlace.hasPassed(8)
                    && data.playerInfo.lastVelocity.hasPassed(10)
                    && deltaPredict > 0.0001) {
                if(++vl > (data.lagInfo.lastPacketDrop.hasPassed(5) ? 2.5 : 4)) {
                    flag("dY=%v.3 p=%v.3 dx=%v.3", data.playerInfo.deltaY, predicted, data.playerInfo.deltaXZ);
                }
            } else vl-= vl > 0 ? 0.2f : 0;

            debug("deltaY=" + data.playerInfo.deltaY + " predicted=" + predicted
                    + " ground=" + data.playerInfo.clientGround + " vl=" + vl);
        }
    }
}
