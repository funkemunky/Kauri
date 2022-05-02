package dev.brighten.anticheat.check.impl.combat.autoclicker;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockPlacePacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.math.cond.MaxDouble;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;

@CheckInfo(name = "Autoclicker (C)", description = "Checks for blatant blocking patterns.",
        checkType = CheckType.AUTOCLICKER, devStage = DevStage.ALPHA, punishVL = 30, maxVersion = ProtocolVersion.V1_8_9)
@Cancellable(cancelType = CancelType.INTERACT)
public class AutoclickerC extends Check {

    private long lastArm;
    private double cps;
    private boolean blocked;
    private int armTicks;
    private MaxDouble verbose = new MaxDouble(40);

    @Packet
    public void onArm(WrappedInArmAnimationPacket packet, long timeStamp) {
        if(data.playerInfo.breakingBlock || data.playerInfo.lookingAtBlock) return;
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
                        flag("arm=%s cps=%.3f lagging=%s", armTicks,
                                cps, data.lagInfo.lagging);
                    }
                } else verbose.subtract(20);
                debug("cps=%s arm=%s lagging=%s vl=%s", cps, armTicks, data.lagInfo.lagging, vl);
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

