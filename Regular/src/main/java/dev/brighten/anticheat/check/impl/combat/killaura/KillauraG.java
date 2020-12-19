package dev.brighten.anticheat.check.impl.combat.killaura;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutRelativePosition;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Killaura (G)", checkType = CheckType.KILLAURA)
public class KillauraG extends Check {

    private boolean negative = false;
    private int tick;

    @Packet
    public void onRelMove(WrappedOutRelativePosition packet) {
        if(data.target != null && packet.getId() == data.target.getEntityId()) {
            packet.setX((byte)packet.getX() + (byte)1);
            debug("sent rel move");
        }
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(packet.isLook() && data.playerInfo.lastAttack.isNotPassed(1)) {
            debug("yaw=%v pitch=%v", packet.getYaw(), packet.getPitch());
        }
    }


}
