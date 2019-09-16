package dev.brighten.anticheat.check.impl.movement.nofall;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;

@CheckInfo(name = "NoFall", description = "Checks to make sure the ground packet from the client is legit.")
public class NoFall extends Check {

    @Packet
    public void onPacket(WrappedInFlyingPacket packet) {
        boolean onGround = data.playerInfo.serverGround;
        if(packet.isGround() != onGround && !data.playerInfo.generalCancel) {
            if(vl++ > 30) {
                punish();
            } else if(vl > 6) {
                flag("client=" + packet.isGround() + " server=" + onGround);
            }
        } else vl-= vl > 0 ? 0.5 : 0;

        if(data.playerInfo.deltaX != 0 || data.playerInfo.deltaY != 0 || data.playerInfo.deltaZ != 0) {
            debug("vl=" + vl + " client=" + packet.isGround() + " server=" + onGround);
        }
    }
}
