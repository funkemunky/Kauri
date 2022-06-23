package dev.brighten.anticheat.check.impl.combat.autoclicker;

import cc.funkemunky.api.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientAnimation;
import cc.funkemunky.api.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;
import cc.funkemunky.api.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import cc.funkemunky.api.io.github.retrooper.packetevents.util.SpigotConversionUtil;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
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
    public void onArm(WrapperPlayClientAnimation packet, long timeStamp) {
        if(data.playerInfo.breakingBlock || data.playerInfo.lookingAtBlock) return;
        cps = 1000D / (timeStamp - lastArm);
        lastArm = timeStamp;
        armTicks++;
    }

    @Packet
    public void onFlying(WrapperPlayClientPlayerFlying packet) {
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
    public void onPlace(WrapperPlayClientPlayerBlockPlacement packet) {
        if(packet.getItemStack()
                .map(i -> SpigotConversionUtil.toBukkitItemStack(i).getType().name().contains("SWORD"))
                .orElse(false)) return;
        blocked = true;
    }
}

