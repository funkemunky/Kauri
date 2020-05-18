package dev.brighten.anticheat.check.impl.movement.speed;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Speed (D)", description = "Checks for omni sprint.", checkType = CheckType.SPEED,
        developer = true, punishVL = 100)
@Cancellable
public class SpeedD extends Check {

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        if(packet.isPos() && !data.playerInfo.generalCancel) {
            if(data.playerInfo.sprinting
                    && timeStamp - data.playerInfo.lastServerPos > 150
                    && data.playerInfo.clientGround
                    && !data.blockInfo.collidesHorizontally
                    && !data.predictionService.key.equals("Nothing")
                    && !data.predictionService.key.contains("W")) {
                vl++;
                if(vl > 40) {
                    flag("key=%v groundTicks=%v lastVelocity=%v",
                            data.predictionService.key,
                            data.playerInfo.groundTicks,
                            data.playerInfo.lastVelocity.getPassed());
                }
            } else vl-= vl > 0 ? 0.5f : 0;
            debug("sprinting=" + data.playerInfo.sprinting
                    + " ground=" + data.playerInfo.clientGround
                    + " key=" + data.predictionService.key
                    + " velocity=" + data.playerInfo.lastVelocity.getPassed() + " vl=" + vl);
        } else vl-= vl > 0 ? 0.01f : 0;
        //debug("general=" + data.playerInfo.generalCancel);
    }

}
