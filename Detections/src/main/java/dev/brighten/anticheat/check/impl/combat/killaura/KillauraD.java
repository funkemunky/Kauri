package dev.brighten.anticheat.check.impl.combat.killaura;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;

@CheckInfo(name = "Killaura (D)", description = "Checks if a user attacks while inventory is open.",
        checkType = CheckType.KILLAURA, devStage = DevStage.BETA)
public class KillauraD extends Check {

    @Packet
    public void onPacket(WrappedInFlyingPacket packet) {
        if((packet.isPos() || packet.isLook())
                && data.playerInfo.lastWindowClick.isNotPassed(3)
                && data.playerInfo.lastAttack.isNotPassed(1)
                && data.playerInfo.inventoryOpen) {
            vl++;
            flag("window=%s attack=%s",
                    data.playerInfo.lastWindowClick.getPassed(), data.playerInfo.lastAttack.getPassed());
        }
    }
}
