package dev.brighten.anticheat.check.impl.movement.inv;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInWindowClickPacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;

@CheckInfo(name = "Inventory (D)", description = "Clicking in inventory while not open.",
        checkType = CheckType.INVENTORY, devStage = DevStage.ALPHA)
@Cancellable(cancelType = CancelType.INVENTORY)
public class InventoryD extends Check {

    @Packet
    public void onWindow(WrappedInWindowClickPacket packet) {
        if (data.playerInfo.generalCancel) return;

        if (packet.getId() == 0 && !data.playerInfo.inventoryOpen) {
            vl++;
            flag(80, "id=%s", packet.getId());
        }
    }
}
