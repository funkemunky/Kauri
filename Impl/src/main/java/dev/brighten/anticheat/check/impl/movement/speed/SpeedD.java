package dev.brighten.anticheat.check.impl.movement.speed;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.MovementUtils;
import dev.brighten.api.check.CheckType;
import lombok.val;

@CheckInfo(name = "Speed (D)", description = "Retarded ground acceleration check.", checkType = CheckType.SPEED,
        punishVL = 20)
public class SpeedD extends Check {

    private int moveTicks;
    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        if(packet.isPos() && !data.playerInfo.serverPos) {
            if(data.playerInfo.clientGround && data.playerInfo.serverGround) {
                moveTicks++;

                val accel = data.playerInfo.deltaXZ - data.playerInfo.lDeltaXZ;
                val baseSpeed = MovementUtils.getBaseSpeed(data);

                if((accel > 0.1f || accel < -0.15f)
                        && (data.playerInfo.deltaXZ > baseSpeed || data.playerInfo.lDeltaXZ > baseSpeed)) {
                    debug(Color.Green + "Flag");
                    if(vl++ > 1) {
                        flag("accel=" + accel);
                    }
                } else vl-= vl > 0 ? 0.2f : 0;
                debug("accel=" + accel + " vl=" + vl);
            } else  moveTicks = 0;
        } else moveTicks = 0;
    }

}
