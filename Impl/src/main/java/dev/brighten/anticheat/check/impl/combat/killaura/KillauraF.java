package dev.brighten.anticheat.check.impl.combat.killaura;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.api.check.CheckType;
import lombok.val;
import org.bukkit.entity.Player;

@CheckInfo(name = "Killaura (F)", description = "Checks for proper sprint motion mechanics.",
        checkType = CheckType.KILLAURA, developer = true)
@Cancellable(cancelType = CancelType.ATTACK)
public class KillauraF extends Check {

    private int buffer;
    private boolean attack;

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(attack && data.playerInfo.sprinting) {
            double px = data.playerInfo.lDeltaX, pz = data.playerInfo.lDeltaZ;

            px*= 0.6;
            pz*= 0.6;

            double pxz = Math.hypot(px, pz), noxz = data.playerInfo.lDeltaXZ;

            double deltaYes = Math.abs(data.playerInfo.deltaXZ - pxz),
                    deltaNo = Math.abs(data.playerInfo.deltaXZ - noxz);

            if(deltaYes > 0.07 && deltaNo < 0.01) {
                if(++buffer > 5) {
                     vl++;
                     flag("dy=%v.3 dn=%v.3 dxz=%v.2 noxz=%v.2",
                             deltaYes, deltaNo, data.playerInfo.deltaXZ, noxz);
                }
            } else if(buffer > 0) buffer--;

            debug("(%v) dxz=%v.3 pxz=%v.3 noxz=%v.3 dYes=%v.3 dNo=%v.3",
                    buffer, data.playerInfo.deltaXZ, pxz, noxz, deltaYes, deltaNo);
        }
        attack = false;
    }

    @Packet
    public void onUse(WrappedInUseEntityPacket packet) {
        if(packet.getAction() == WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK
                && packet.getEntity() instanceof Player) {
            attack = true;
        }
    }
}
