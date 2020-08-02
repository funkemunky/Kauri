package dev.brighten.anticheat.check.impl.combat.killaura;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.api.check.CheckType;
import org.bukkit.entity.Player;

@CheckInfo(name = "Killaura (F)", description = "Checks for proper sprint motion mechanics.",
        checkType = CheckType.KILLAURA, developer = true)
@Cancellable(cancelType = CancelType.ATTACK)
public class KillauraF extends Check {

    private int buffer;
    @Packet
    public void onUse(WrappedInUseEntityPacket packet) {
        if(packet.getAction() == WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK) {
            if(data.playerInfo.sprinting && data.target instanceof Player) {
                double accel = Math.abs(data.playerInfo.deltaXZ - data.playerInfo.lDeltaXZ);

                if(accel < 0.0005) {
                    if(++buffer > 2) {
                        vl++;
                        flag("accel=%v.4", accel);
                    }
                } else if(buffer > 0) buffer--;
                debug("accel=%v", accel);
            }
        }
    }
}
