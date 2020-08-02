package dev.brighten.anticheat.check.impl.combat.hitbox;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.api.check.CheckType;
import org.bukkit.entity.Entity;

@CheckInfo(name = "Hitboxes", description = "Checks if the player attacks outside a player's hitbox.",
        checkType = CheckType.HITBOX, punishVL = 15, developer = true)
@Cancellable(cancelType = CancelType.ATTACK)
public class Hitboxes extends Check {

    private boolean useEntity;
    private Entity target;

    @Packet
    public void onUse(WrappedInUseEntityPacket packet) {
        if(packet.getAction().equals(WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK)) {
            useEntity = true;
            target = packet.getEntity();
        }
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(useEntity) {
            checkProcessing: {
            
            }

            useEntity = false;
        }
    }

}