package dev.brighten.anticheat.check.impl.premium;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInWindowClickPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;

@CheckInfo(name = "Inventory (A)", description = "Checks if a player clicks in their inventory while moving.",
        checkType = CheckType.INVENTORY, devStage = DevStage.ALPHA)
public class InventoryA extends Check {

    private int moveStreak;

    @Override
    public void setData(ObjectData data) {
        super.setData(data);
    }

    @Packet
    public void onWindow(WrappedInWindowClickPacket packet) {
        if(data.playerInfo.lastFlyingTimer.isPassed(2)) moveStreak = 0;
        if(moveStreak > 5 && data.playerInfo.lastVelocity.isPassed(20))  {
            vl++;
            flag("slot=%s clickType=%s ms=%s o=%s", packet.getSlot(), packet.getAction().name(), moveStreak,
                    data.playerInfo.inventoryOpen);
        }
    }

    @Packet
    public void onFlyng(WrappedInFlyingPacket packet) {
        if(packet.isPos()
                && data.playerInfo.deltaXZ > 0
                && data.playerInfo.liquidTimer.isPassed(2)
                && data.playerInfo.climbTimer.isPassed(3)
                && (data.playerInfo.serverGround && data.playerInfo.clientGround)
                && !data.getPlayer().isDead()
                && data.playerInfo.lastTeleportTimer.isPassed(5)
                && data.playerInfo.lastVelocity.isPassed(20)) {
            moveStreak++;
        } else moveStreak = 0;
    }
}
