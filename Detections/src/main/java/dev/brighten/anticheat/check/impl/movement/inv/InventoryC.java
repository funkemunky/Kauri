package dev.brighten.anticheat.check.impl.movement.inv;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInWindowClickPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Inventory (C)", description = "Checks for invalid window clicks.",
        checkType = CheckType.INVENTORY)
public class InventoryC extends Check {

    private long lastFlying;

    @Packet
    public void use(WrappedInWindowClickPacket packet, long current) {
        if(current - lastFlying < 10) {
            vl++;
            if(vl > 11) {
                flag("delta=%s", current - lastFlying);
            }
        } else if(vl > 0) vl--;
    }

    @Packet
    public void flying(WrappedInFlyingPacket packet, long current) {
        if(data.playerInfo.lastTeleportTimer.isPassed(0))
            lastFlying = current;
    }
}
