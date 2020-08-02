package dev.brighten.anticheat.check.impl.combat.killaura;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockPlacePacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.anticheat.data.ObjectData;
import cc.funkemunky.api.utils.TickTimer;
import dev.brighten.api.check.CheckType;
import org.bukkit.entity.EntityType;

@CheckInfo(name = "Killaura (E)", description = "Checks if a player attacks before blocking.",
        checkType = CheckType.KILLAURA, punishVL = 20, vlToFlag = 6, developer = true)
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
            flag(10, "attacked=true interacted=false");
        }
        debug("attacks=%v interacts=%v", attacks, interacts);
    }

}
