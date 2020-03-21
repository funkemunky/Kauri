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

@CheckInfo(name = "Fly (D)", description = "Completely predicts the vertical axis of movement.",
        checkType = CheckType.FLIGHT, punishVL = 40, vlToFlag = 5, developer = true, enabled = false)
@Cancellable
public class FlyD extends Check {

    private double py;

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        /** PRE MOTION CALCULATION **/
        boolean didBox = false;
        if(!packet.isPos()) py = 0;
        else {
            /* y=0 calculations */
            if ((ProtocolVersion.getGameVersion().isOrBelow(ProtocolVersion.V1_8_9) && Math.abs(py) < 0.005))
                py = 0;
            /* end y=0 calculations */

            /** MOTION CALCULATION **/
            if (data.playerInfo.lClientGround
                    && !data.playerInfo.clientGround
                    && data.playerInfo.deltaY > 0) {
                py = MovementUtils.getJumpHeight(data.getPlayer());

                if (Math.abs(data.playerInfo.deltaY - py) > 1E-6) {
                    double test = py - 0.015625;

                    if (Math.abs(data.playerInfo.deltaY - py) > Math.abs(test - data.playerInfo.deltaY) + 1E-5) {
                        py = test;
                        didBox = true;
                    }
                }
            }

            /** POSITION ALCULATION **/
            /* collision algorithm */
            double dh = Math.min(data.playerInfo.deltaXZ, 1), dy = data.playerInfo.deltaY;
            for (Block block : Helper.blockCollisions(data.blockInfo.handler.getBlocks(),
                    data.box.copy().expand(1 + dh, 0.35 + Math.min(1, Math.abs(dy)), 1 + dh))) {
                CollisionBox box = BlockData.getData(block.getType())
                        .getBox(block, ProtocolVersion.getGameVersion());

                List<SimpleCollisionBox> sBoxes = new ArrayList<>();
                box.downCast(sBoxes);

                for (SimpleCollisionBox sBox : sBoxes) {
                    double minDelta = sBox.yMax - data.playerInfo.from.y,
                            maxDelta = sBox.yMin - (data.playerInfo.from.y + 1.8);

                    double mind = MathUtils.getDelta(data.playerInfo.deltaY, minDelta),
                            maxd = MathUtils.getDelta(data.playerInfo.deltaY, maxDelta);

                    if (packet.isPos() && (py != 0 || data.playerInfo.deltaXZ > 0))
                        debug("mind=%v maxd=%v md=%v", mind, maxd, maxDelta);
                    if (mind < 1E-7) {
                        py = minDelta;
                        didBox = false;
                        break;
                    } else if (maxd < 1E-7) {
                        py = maxDelta;
                        didBox = false;
                        break;
                    }
                }
            }
            /* end collision algorithm */

        }
        //Set prediction y to current delta calculations
        if (data.playerInfo.lastVelocity.hasNotPassed(5)
                || data.playerInfo.serverPos
                || data.playerInfo.blockAboveTimer.hasNotPassed(1)
                || data.playerInfo.lastRespawnTimer.hasNotPassed(0))
            py = data.playerInfo.deltaY;
        //End Set prediction y to current delta calculations

        //flag check
        if(!data.playerInfo.flightCancel && packet.isPos()
                && data.playerInfo.lastVelocity.hasPassed(8)
                && data.playerInfo.lastBlockPlace.hasPassed(6)
                && Math.abs(data.playerInfo.deltaY - py) > 0.001) {
            vl++;
            flag("dy=%v.4 py=%v.4", data.playerInfo.deltaY, py);
        } else vl-= vl > 0 ? 0.005 : 0;
        //debugging
        if(packet.isPos() && (py != 0 || data.playerInfo.deltaXZ > 0))
        debug("deltaY=%v predicted=%v", data.playerInfo.deltaY, py);
        else debug("flying");

        /** POST-MOTION ALCULATION **/

        if(didBox) py = MovementUtils.getJumpHeight(data.getPlayer());
        //Deceleration algorithim
        py = (py - 0.08) * 0.98;
    }

}
