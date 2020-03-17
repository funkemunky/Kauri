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
    public void onFlying(WrappedInFlyingPacket packet) {
        /** PRE MOTION CALCULATION **/
        /* y=0 calculations */
        if((ProtocolVersion.getGameVersion().isOrBelow(ProtocolVersion.V1_8_9) && Math.abs(py) < 0.005)
                || data.playerInfo.serverPos
                || data.playerInfo.lastRespawnTimer.hasNotPassed(0))
            py = 0;
        /* end y=0 calculations */

        //Setting velocity stuff.
        if(data.playerInfo.lastVelocity.hasNotPassed(0))
            py = data.playerInfo.velocityY;
        //End setting velocity stuff.

        /** MOTION CALCULATION **/
        boolean didBox = false;
        if(data.playerInfo.lClientGround
                && !data.playerInfo.clientGround
                && data.playerInfo.deltaY > 0) {
            py = MovementUtils.getJumpHeight(data.getPlayer());

            if(Math.abs(data.playerInfo.deltaY - py) > 1E-6) {
                double test = py - 0.015625;

                if(Math.abs(data.playerInfo.deltaY - py) > Math.abs(test - data.playerInfo.deltaY) + 1E-5) {
                    py = test;
                    didBox = true;
                }
            }
        }

        /** POSITION ALCULATION **/
        /* collision algorithm */
        double dh = data.playerInfo.deltaXZ , dy = data.playerInfo.deltaY;
        for (Block block : Helper.blockCollisions(data.blockInfo.handler.getBlocks(),
                data.box.copy().expand(0.5 + dh, 0.5 + Math.abs(dy), 0.5 + dh))) {
            CollisionBox box = BlockData.getData(block.getType())
                    .getBox(block, ProtocolVersion.getGameVersion());

            List<SimpleCollisionBox> sBoxes = new ArrayList<>();
            box.downCast(sBoxes);

            for (SimpleCollisionBox sBox : sBoxes) {
                double minDelta = sBox.yMax - data.playerInfo.from.y,
                        maxDelta = sBox.yMin - (data.playerInfo.from.y + 1.8);

                double mind = MathUtils.getDelta(data.playerInfo.deltaY, minDelta),
                        maxd = MathUtils.getDelta(data.playerInfo.deltaY, maxDelta);

                if(packet.isPos() && (py != 0 || data.playerInfo.deltaXZ > 0))
                debug("mind=%v maxd=%v", mind, maxd);
                if(mind < 1E-7) {
                    py = minDelta;
                    didBox = false;
                    break;
                } else if(maxd < 1E-7) {
                    py = maxDelta;
                    didBox = false;
                    break;
                }
            }
        }
        /* end collision algorithm */

        //flag check
        if(!data.playerInfo.flightCancel && packet.isPos()
                && Math.abs(data.playerInfo.deltaY - py) > 0.001) {
            vl++;
            flag("dy=%v.4 py=%v.4", data.playerInfo.deltaY, py);
        } else vl-= vl > 0 ? 0.005 : 0;
        //debugging
        if(packet.isPos() && (py != 0 || data.playerInfo.deltaXZ > 0))
        debug("deltaY=%v predicted=%v", data.playerInfo.deltaY, py);

        /** POST-MOTION ALCULATION **/

        if(didBox) py = MovementUtils.getJumpHeight(data.getPlayer());
        //Deceleration algorithim
        py = (py - 0.08) * 0.98;
    }

}
