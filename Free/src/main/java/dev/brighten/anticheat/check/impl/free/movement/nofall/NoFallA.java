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

@CheckInfo(name = "NoFall (A)", description = "Looks for impossible location updates compared to ground.",
        checkType = CheckType.NOFALL, punishVL = 12, vlToFlag = 2, executable = true, planVersion = KauriVersion.FREE)
@Cancellable
public class NoFallA extends Check {

    private float buffer;

    @Packet
    public void onPacket(WrappedInFlyingPacket packet) {

        boolean flag = data.playerInfo.clientGround
                ? Math.abs(data.playerInfo.deltaY) > 0.007
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
                && !data.blockInfo.inScaffolding
                && !data.blockInfo.inHoney
                && !data.blockInfo.blocksAbove
                && data.playerInfo.lastGhostCollision.isPassed(2)
                && data.playerInfo.lastVelocity.isPassed(4)
                && (data.playerInfo.deltaY != 0 || data.playerInfo.deltaXZ > 0)
                && data.playerInfo.blockAboveTimer.isPassed(10)
                && flag) {

            if(++buffer > 2) {
                vl++;
                flag("g=%s dy=%.4f", data.playerInfo.clientGround, data.playerInfo.deltaY);
            }
        } else buffer-= buffer > 0 ? 0.25f : 0;

        debug("ground=" + data.playerInfo.clientGround + " collides=" + data.playerInfo.serverGround
                + " deltaY=" + data.playerInfo.deltaY + " vl=" + vl);
    }
}