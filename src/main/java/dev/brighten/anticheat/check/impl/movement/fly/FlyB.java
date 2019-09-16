package dev.brighten.anticheat.check.impl.movement.fly;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.MovementUtils;

@CheckInfo(name = "Fly (B)", description = "Ensures the player doesn't exceed max motion;")
public class FlyB extends Check {

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(!packet.isPos()) return;
        float max = MovementUtils.getJumpHeight(data.getPlayer()) + 0.01f;

        if(data.playerInfo.deltaY > max) {
            vl++;

            if(vl > 1) {
                flag(data.playerInfo.deltaY + ">-" + max);
            }
        } else vl-= vl > 0 ? 0.02 : 0;
    }

}
