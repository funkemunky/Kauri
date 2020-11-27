package dev.brighten.anticheat.premium.impl;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInWindowClickPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.utils.timer.Timer;
import dev.brighten.anticheat.utils.timer.impl.PlayerTimer;
import dev.brighten.api.KauriVersion;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Inventory (A)", description = "Checks if a player clicks in their inventory while moving.",
        checkType = CheckType.INVENTORY, developer = true, planVersion = KauriVersion.ARA)
public class InventoryA extends Check {

    private Timer lastMove;

    @Override
    public void setData(ObjectData data) {
        lastMove = new PlayerTimer(data);
        super.setData(data);
    }

    @Packet
    public void onWindow(WrappedInWindowClickPacket packet) {
        if(lastMove.isNotPassed(1))  {
            vl++;
            flag("slot=%v clickType=%v", packet.getSlot(), packet.getAction().name());
        }
    }

    @Packet
    public void onFlyng(WrappedInFlyingPacket packet) {
        if(packet.isPos()
                && (data.playerInfo.deltaXZ > 0 || data.playerInfo.deltaY != 0)
                && data.playerInfo.liquidTimer.isPassed(2)
                && data.playerInfo.climbTimer.isPassed(3)
                && (data.playerInfo.serverGround
                || (data.predictionService.moveForward != 0
                && data.playerInfo.deltaXZ > 0.2
                && data.predictionService.moveStrafing != 0))
                && !data.getPlayer().isDead()
                && data.playerInfo.lastTeleportTimer.isPassed(5)
                && data.playerInfo.lastVelocity.isPassed(20)) {
            lastMove.reset();
        }
    }
}
