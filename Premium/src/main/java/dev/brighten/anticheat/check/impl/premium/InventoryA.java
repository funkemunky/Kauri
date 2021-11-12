package dev.brighten.anticheat.check.impl.premium;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInClientCommandPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInCloseWindowPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInWindowClickPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutOpenWindow;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.api.KauriVersion;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;

@CheckInfo(name = "Inventory (A)", description = "Checks if a player clicks in their inventory while moving.",
        checkType = CheckType.INVENTORY, devStage = DevStage.CANARY, planVersion = KauriVersion.ARA)
public class InventoryA extends Check {

    private int moveStreak;
    private int openInventory;

    @Override
    public void setData(ObjectData data) {
        super.setData(data);
    }

    @Packet
    public void onWindow(WrappedInWindowClickPacket packet) {
        if(moveStreak > 5 && data.playerInfo.lastVelocity.isPassed(20))  {
            vl++;
            flag("slot=%s clickType=%s ms=%s", packet.getSlot(), packet.getAction().name(), moveStreak);
        }
    }

    @Packet
    public void onInventoryOpen(WrappedOutOpenWindow packet) {
        data.runKeepaliveAction(ka -> {
            openInventory = packet.getId();
            debug("opened server inventory id=" + packet.getId());
        });
    }

    @Packet
    public void onClose(WrappedInCloseWindowPacket packet) {
        debug("closed inventory: open=%s nowClosing=%s", openInventory, packet.getId());
        openInventory = -69;
    }

    @Packet
    public void onClientCommand(WrappedInClientCommandPacket packet) {
        if(packet.getCommand() == WrappedInClientCommandPacket.EnumClientCommand.OPEN_INVENTORY_ACHIEVEMENT
        && !data.blockInfo.inPortal) {
            openInventory = 0;
            debug("opened inventory id=0");
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
