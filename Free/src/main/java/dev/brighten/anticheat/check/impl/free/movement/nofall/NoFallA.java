package dev.brighten.anticheat.check.impl.free.movement.nofall;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.KauriVersion;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "NoFall (A)", description = "Checks to make sure the ground packet from the client is legit",
        checkType = CheckType.NOFALL, punishVL = 20, vlToFlag = 3, executable = false, planVersion = KauriVersion.FREE)
@Cancellable
public class NoFallA extends Check {

    @Packet
    public void onPacket(WrappedInFlyingPacket packet, long timeStamp) {

        boolean flag = data.playerInfo.clientGround
                ? data.playerInfo.deltaY != 0
                && (Math.abs(data.playerInfo.deltaY) >= Math.abs(data.playerInfo.lDeltaY))
                && !data.playerInfo.serverGround
                && data.playerInfo.lastBlockPlace.isPassed(10)
                : data.playerInfo.deltaY == 0 && data.playerInfo.lDeltaY == 0;

        if(data.playerInfo.deltaY < 0 && data.playerInfo.clientGround && flag) {
            for (SimpleCollisionBox sBox : data.blockInfo.belowCollisions) {
                double minDelta = sBox.yMax - data.playerInfo.from.y;

                if(MathUtils.getDelta(data.playerInfo.deltaY, minDelta) < 1E-7) {
                    flag = false;
                    break;
                }
            }
        }

        if(!data.playerInfo.flightCancel
                && data.playerInfo.lastHalfBlock.isPassed(4)
                && !data.blockInfo.onSlime
                && !data.blockInfo.blocksAbove
                && data.playerInfo.lastBlockPlace.isPassed(8)
                && data.playerInfo.lastVelocity.isPassed(4)
                && (data.playerInfo.deltaY != 0 || data.playerInfo.deltaXZ > 0)
                && data.playerInfo.blockAboveTimer.isPassed(10)
                && flag) {

            if(++vl > 2) {
                flag("ground=" + data.playerInfo.clientGround + " deltaY=" + data.playerInfo.deltaY);
            }
        } else vl-= vl > 0 ? 0.2f : 0;

        debug("ground=" + data.playerInfo.clientGround + " collides=" + data.playerInfo.serverGround
                + " deltaY=" + data.playerInfo.deltaY + " vl=" + vl);
    }
}