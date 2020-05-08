package dev.brighten.anticheat.check.impl.movement.speed;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;

@CheckInfo(name = "Test", description = "Test")
public class Test extends Check {

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        String type = "";

        if(packet.isPos() && packet.isLook()) type = "posLook";
        else if(packet.isPos()) type = "pos";
        else if(packet.isLook()) type = "look";
        else type = "flying";
        debug("type=%v timestamp=%v deltaY=%v.3", type,  timeStamp, data.playerInfo.deltaY);
    }
}
