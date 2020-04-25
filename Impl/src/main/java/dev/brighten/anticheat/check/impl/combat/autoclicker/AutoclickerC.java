package dev.brighten.anticheat.check.impl.combat.autoclicker;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.*;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.math.cond.MaxDouble;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Autoclicker (C)", description = "Checks for blatant blocking patterns.",
        checkType = CheckType.AUTOCLICKER, developer = true, punishVL = 200, maxVersion = ProtocolVersion.V1_8_9)
@Cancellable(cancelType = CancelType.INTERACT)
public class AutoclickerC extends Check {

    private long lastArm;
    private double cps;
    private boolean blocked;
    private int armTicks;
    private MaxDouble verbose = new MaxDouble(40);

    @Packet
    public void onArm(WrappedInArmAnimationPacket packet, long timeStamp) {
        cps = 1000D / (timeStamp - lastArm);
        lastArm = timeStamp;
        armTicks++;
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(blocked) {
            if(armTicks > 0) {
                if(armTicks == 1 && cps > 3) {
                    if(cps > 7) verbose.add();
                    if(verbose.value() > 15) {
                        flag("arm=%v cps=%v lagging=%v", armTicks,
                                MathUtils.round(cps, 3), data.lagInfo.lagging);
                    }
                } else verbose.subtract(20);
                debug("cps=%v arm=%v lagging=%v vl=%v", cps, armTicks, data.lagInfo.lagging, vl);
            }
            blocked = false;
            armTicks = 0;
        }
    }

    @Packet
    public void onPlace(WrappedInBlockPlacePacket packet) {
        if(packet.getItemStack() == null || !packet.getItemStack().getType().name().contains("SWORD")) return;
        blocked = true;
    }
}

