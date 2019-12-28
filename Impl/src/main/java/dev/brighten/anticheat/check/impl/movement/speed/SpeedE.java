package dev.brighten.anticheat.check.impl.movement.speed;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MathHelper;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.MovementUtils;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Speed (E)", description = "The maximum speed a player can possibly move.",
        checkType = CheckType.SPEED, punishVL = 40, developer = true)
public class SpeedE extends Check {

    private int verbose;
    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        if(packet.isPos() && !data.playerInfo.serverPos && timeStamp - data.creation > 200L) {
            if(!data.predictionService.walkSpecial && data.predictionService.checkConditions && !data.blockInfo.collidesHorizontally) {
                double pred = MathUtils.hypot(data.predictionService.predX, data.predictionService.predZ);
                double delta = MathUtils.getDelta(pred, data.playerInfo.deltaXZ);

                if(delta > 0.0001 && !data.playerInfo.generalCancel) {
                    if(verbose++ > 10) {
                        vl++;
                        flag("delta=" + delta + " vb=" + verbose + " ping=%p");
                    }
                } else verbose-= verbose > 0 ? 2 : 0;

                debug("pred=" + pred + " deltaXZ=" + data.playerInfo.deltaXZ + " walk=" + (float) (data.getPlayer().getWalkSpeed() / 2D)
                        + " ai=" + data.predictionService.aiMoveSpeed + " pl=" + Atlas.getInstance().getBlockBoxManager().getBlockBox().getAiSpeed(data.getPlayer()));
            }
        }
    }
}
