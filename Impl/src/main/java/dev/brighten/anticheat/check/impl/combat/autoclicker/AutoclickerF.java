package dev.brighten.anticheat.check.impl.combat.autoclicker;

import cc.funkemunky.api.tinyprotocol.api.TinyProtocolHandler;
import cc.funkemunky.api.tinyprotocol.packet.in.*;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutHeldItemSlot;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Autoclicker (F)", description = "Checks for common blocking patterns.",
        checkType = CheckType.AUTOCLICKER, developer = true, punishVL = 200)
public class AutoclickerF extends Check {

    private long lastArm;
    private double cps;
    private boolean blocked, blocking;
    private int armTicks, slot;

    @Packet
    public void onArm(WrappedInArmAnimationPacket packet, long timeStamp) {
        cps = 1000D / (timeStamp - lastArm);
        lastArm = timeStamp;
        armTicks++;
    }

    @Packet
    public void onUse(WrappedInUseEntityPacket packet) {
        if(packet.getAction().equals(WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK) && blocking) {
            TinyProtocolHandler.sendPacket(packet.getPlayer(), new WrappedOutHeldItemSlot(slot == 8 ? 0 : Math.min(8, slot + 1)).getObject());
            debug("unblocked");
        }
    }

    @Packet
    public void onDig(WrappedInBlockDigPacket packet) {
        if(packet.getAction().name().contains("DROP")
                || packet.getAction().equals(WrappedInBlockDigPacket.EnumPlayerDigType.RELEASE_USE_ITEM)) {
            blocking = false;
        }
    }

    @Packet
    public void onHeld(WrappedInHeldItemSlotPacket packet) {
        blocking = false;
        slot = packet.getSlot();
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(blocked) {
            if(armTicks > 0) {
                if(armTicks == 1 && cps > 3) {
                    if(cps > 8) vl++;
                    if(vl > 15) {
                        flag("arm=%1 cps=%2 lagging=%3", armTicks, MathUtils.round(cps, 3), data.lagInfo.lagging);
                    }
                } else vl = 0;
                debug("cps=%1 arm=%2 lagging=%3 vl=%4", cps, armTicks, data.lagInfo.lagging, vl);
            }
            blocked = false;
            armTicks = 0;
        }
    }

    @Packet
    public void onPlace(WrappedInBlockPlacePacket packet) {
        blocked = blocking =  true;
    }
}

