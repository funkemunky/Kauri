package dev.brighten.anticheat.check.impl.movement.fly;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.KauriVersion;
import org.bukkit.potion.PotionEffectType;

@CheckInfo(name = "Fly (G)", description = "This checks for step my guy", developer = true,
        planVersion = KauriVersion.FULL)
@Cancellable
public class FlyG extends Check {

    private double stepHeight;
    private long lastFlying;

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long now) {
        if(MathUtils.getDelta(data.playerInfo.to.y, data.playerInfo.from.y) < 1
                && data.playerInfo.from.y != 0 && !data.playerInfo.flightCancel) {
            if(!data.playerInfo.clientGround && packet.isPos() && now - lastFlying < 90) {
                stepHeight+= data.playerInfo.deltaY;
            } else if(data.playerInfo.deltaY == 0.42 || data.playerInfo.deltaY == 1.0) {
                vl++;
                flag("this is embarassing");
            } else if(data.playerInfo.deltaY > 0
                    && (data.playerInfo.lastHalfBlock.isPassed(5)
                    || MathUtils.getDelta(data.playerInfo.deltaY, 0.5) > 0.01)) {
                vl++;
                flag("oop [%v.4]", data.playerInfo.deltaY);
            } else {
                debug("h=%v", stepHeight);

                if(stepHeight > 0 && !data.potionProcessor.hasPotionEffect(PotionEffectType.JUMP)) {
                    if (stepHeight > 1.3) {
                        vl++;
                        flag("too high buddy dY=%v.2", stepHeight);
                    } else if(stepHeight > 1 && stepHeight < 1.015) {
                        vl++;
                        flag("wrongo");
                    } else if(stepHeight % 1. == 0) {
                        vl++;
                        flag("you're a magician");
                    }
                }


                stepHeight = 0;
            }
        }
        lastFlying = now;
    }
}
