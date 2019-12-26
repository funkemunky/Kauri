package dev.brighten.anticheat.check.impl.movement.speed;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathHelper;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.MovementUtils;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Speed (E)", description = "The maximum speed a player can possibly move.",
        checkType = CheckType.SPEED, punishVL = 5)
public class SpeedE extends Check {

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        if(packet.isPos() && !data.playerInfo.serverPos && timeStamp - data.creation > 200L) {
            float base = MovementUtils.getBaseSpeed(data) * 2.3f;
            double mult = Math.pow(base, 2) / 2;
            double x = MathUtils.sqrt(mult), z = x;

            x -= (MathHelper.sin(0) * 0.20000000298023224D);
            z += (MathHelper.cos(0) * 0.20000000298023224D);

            double max = MathUtils.hypot(x, z) + 0.24f;

            if(data.playerInfo.deltaXZ > max && !data.playerInfo.generalCancel) {
                vl++;
                flag(data.playerInfo.deltaXZ + ">-" + max);
            }

            debug("max=" + max + " deltaXZ=" + data.playerInfo.deltaXZ);
        }
    }
}
