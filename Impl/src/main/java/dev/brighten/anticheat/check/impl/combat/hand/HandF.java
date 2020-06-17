package dev.brighten.anticheat.check.impl.combat.hand;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockPlacePacket;
import cc.funkemunky.api.tinyprotocol.packet.types.enums.WrappedEnumDirection;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Hand (F)", description = "Looks for invalid placement of blocks.",
        checkType = CheckType.HAND, developer = true)
@Cancellable(cancelType = CancelType.PLACE)
public class HandF extends Check {

    @Packet
    public void onPlace(WrappedInBlockPlacePacket packet) {
        if(!packet.getFace().equals(WrappedEnumDirection.UP)
                && !packet.getFace().equals(WrappedEnumDirection.DOWN)) {
            if(Math.abs(data.playerInfo.to.pitch) == 90) {
                vl++;
                flag("type=%v p=%v", "a", data.playerInfo.to.pitch);
            }

            debug("(%v) pitch=%v.1", packet.getFace().name(), data.playerInfo.to.pitch);
        } else {
            if(data.playerInfo.to.pitch == 0) {
                vl++;
                flag("type=%v p=%v", "b", data.playerInfo.to.pitch);
            }
            debug("(%v) pitch=%v.1", packet.getFace().name(), data.playerInfo.to.pitch);
        }
    }
}
