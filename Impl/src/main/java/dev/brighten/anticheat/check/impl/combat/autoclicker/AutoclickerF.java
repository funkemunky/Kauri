package dev.brighten.anticheat.check.impl.combat.autoclicker;
import cc.funkemunky.api.tinyprotocol.api.NMSObject;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockDigPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockPlacePacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.utils.math.cond.MaxInteger;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.anticheat.utils.GraphUtil;
import dev.brighten.api.check.CheckType;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

@CheckInfo(name = "Autoclicker (F)", description = "Checks for consistency through graphical means (Elevated).",
        checkType = CheckType.AUTOCLICKER, punishVL = 50, developer = true, enabled = false)
@Cancellable(cancelType = CancelType.INTERACT)
public class AutoclickerF extends Check {

    @Packet
    public void onWrapped(NMSObject object) {
        if(object instanceof WrappedInArmAnimationPacket) {
            debug("arm");
        } else if(object instanceof WrappedInUseEntityPacket) {
            debug("entity");
        } else if(object instanceof WrappedInBlockPlacePacket) {
            debug("place");
        } else if(object instanceof WrappedInBlockDigPacket) {
            debug("dig");
        }
    }

}
