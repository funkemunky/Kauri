package dev.brighten.anticheat.check.impl.combat.autoclicker;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockPlacePacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Autoclicker (F)", description = "Checks for common blocking patterns.",
        checkType = CheckType.AUTOCLICKER, developer = true, punishVL = 200)
public class AutoclickerF extends Check {

    private long lastArm;
    private double cps;
    private boolean blocked;
    private int armTicks, placeTicks;

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
                if(armTicks == placeTicks) {
                    if(cps > 6) vl++;
                    if(vl > 40) {
                        flag("arm=%1 place=%2 cps=%3", armTicks, placeTicks, cps);
                    }
                } else vl = 0;
                debug("cps=%1 arm=%2 place=%3 vl=%4", cps, armTicks, placeTicks, vl);
            }
            blocked = false;
            placeTicks = armTicks = 0;
        }
    }

    @Packet
    public void onPlace(WrappedInBlockPlacePacket packet) {
        placeTicks++;
        blocked = true;
    }
}

