package dev.brighten.anticheat.premium.impl;

import cc.funkemunky.api.tinyprotocol.api.TinyProtocolHandler;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutCloseWindowPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.KauriVersion;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Inventory (B)", description = "Checks if a player moves while their inventory is open",
        checkType = CheckType.INVENTORY, punishVL = 40, developer = true, enabled = false, planVersion = KauriVersion.ARA)
@Cancellable(cancelType = CancelType.MOVEMENT)
public class InventoryB extends Check {

    private int verbose;

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(packet.isPos()
                && !data.playerInfo.serverPos
                && data.playerInfo.inventoryOpen
                && !data.blockInfo.inLava
                && !data.blockInfo.inWeb
                && data.playerInfo.lastVelocity.isPassed(30)
                && !data.blockInfo.collidesHorizontally
                && (data.predictionService.moveStrafing != 0 || data.predictionService.moveForward != 0)) {
            if(verbose++ > 3) {
                vl++;
                flag("key=[%s], dxz=%s", data.predictionService.key,
                        MathUtils.round(data.playerInfo.deltaXZ, 2));
                if(cancellable) TinyProtocolHandler.sendPacket(packet.getPlayer(),
                        new WrappedOutCloseWindowPacket(data.playerInfo.inventoryId).getObject());
            }
        } else verbose = 0;
    }
}
