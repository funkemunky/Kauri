package dev.brighten.anticheat.premium.impl.autoclicker;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockDigPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockPlacePacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.Color;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Autoclicker (I)", description = "Checks for common auto-blocking patterns.",
        checkType = CheckType.AUTOCLICKER, enabled = false, developer = true)
public class AutoclickerI extends Check {

    private boolean arm, place, flying, dig;
    private int armTicks;
    @Packet
    public void onPacket(WrappedInBlockDigPacket packet, long timeStamp) {
        debug("dig:%1", timeStamp);
        if(flying && armTicks == 0) {
            debug(Color.Green + "Flag");
        }
        armTicks = 0;
        arm = place = flying = dig = false;
    }

    @Packet
    public void onPacket(WrappedInBlockPlacePacket packet, long timeStamp) {
        debug("place:%1", timeStamp);
        if(arm) place = true;
    }

    @Packet
    public void onPacket(WrappedInFlyingPacket packet, long timeStamp) {
        //debug("flying:%1", timeStamp);
        if(place) flying = true;
    }

    @Packet
    public void onPacket(WrappedInArmAnimationPacket packet, long timeStamp) {
        debug("arm:%1", timeStamp);
        if(arm) armTicks++;
        else arm = true;
    }
}
