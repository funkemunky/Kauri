package dev.brighten.anticheat.check.impl.combat.killaura;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockPlacePacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;

@CheckInfo(name = "Killaura (E)", description = "Checks if a player attacks before blocking.",
        checkType = CheckType.KILLAURA, punishVL = 12, vlToFlag = 6, executable = true)
@Cancellable(cancelType = CancelType.ATTACK)
public class KillauraE extends Check {

    private boolean attacks, interacts;

    @Packet
    public void onUse(WrappedInUseEntityPacket packet) {
        switch(packet.getAction()) {
            case ATTACK:
                attacks = true;
                break;
            case INTERACT:
            case INTERACT_AT:
                interacts = true;
                break;
        }
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        attacks = interacts = false;
    }

    @Packet
    public void onBlock(WrappedInBlockPlacePacket packet) {
        if(attacks && !interacts) {
            vl++;
            flag(10, "attacked=true interacted=false");
        }
        debug("attacks=%s interacts=%s", attacks, interacts);
    }

}
