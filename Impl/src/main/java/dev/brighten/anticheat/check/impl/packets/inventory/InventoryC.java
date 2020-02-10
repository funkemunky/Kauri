package dev.brighten.anticheat.check.impl.packets.inventory;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Inventory (C)", description = "Checks if a player moves while their inventory is open",
        checkType = CheckType.INVENTORY, punishVL = 40)
@Cancellable(cancelType = CancelType.MOVEMENT)
public class InventoryC extends Check {

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(packet.isPos()
                && !data.playerInfo.serverPos
                && data.playerInfo.inventoryOpen
                && !data.blockInfo.inLava
                && !data.blockInfo.inWeb
                && data.playerInfo.lastVelocity.hasPassed(5)
                && !data.blockInfo.collidesHorizontally
                && (data.predictionService.moveStrafing != 0 || data.predictionService.moveForward != 0)) {
            vl++;
            flag("key=[%1], dxz=%2", data.predictionService.key,
                    MathUtils.round(data.playerInfo.deltaXZ, 2));
        }
    }
}
