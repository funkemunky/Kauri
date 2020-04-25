package dev.brighten.anticheat.check.impl.combat.killaura;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInWindowClickPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Killaura (D)", description = "Checks if a user attacks while inventory is open.",
        checkType = CheckType.KILLAURA, developer = true)
public class KillauraD extends Check {

    @Packet
    public void onUse(WrappedInUseEntityPacket packet) {
        if(data.playerInfo.lastWindowClick.hasNotPassed(4)) {
            vl++;
            flag("inv=" + data.playerInfo.inventoryId);
        }
    }
}
