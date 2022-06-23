package dev.brighten.anticheat.check.impl.combat.killaura;

import cc.funkemunky.api.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import cc.funkemunky.api.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInCloseWindowPacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Killaura (G)", description = "Closing a window while attacking", checkType = CheckType.KILLAURA,
        punishVL = 2, executable = true)
@Cancellable(cancelType = CancelType.MOVEMENT)
public class KillauraG extends Check {

    private boolean sent;

    @Packet
    public void onUse(WrapperPlayClientInteractEntity packet) {
        if(packet.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK
                && sent
                && data.playerInfo.lastFlyingTimer.isNotPassed(1)) {
            vl++;
            flag(600, "");
        }
    }

    @Packet
    public void onFlying(WrapperPlayClientPlayerFlying packet) {
        sent = false;
    }

    @Packet
    public void onCloseWindow(WrappedInCloseWindowPacket packet) {
        sent = true;
    }
}
