package dev.brighten.anticheat.check.impl.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Aim (D)", description = "Designed to detect aimassists attempting that use cinematic smoothing.",
        checkType = CheckType.AIM, punishVL = 20, developer = true, executable = false)
public class AimD extends Check {

    private int verbose;

    @Packet
    public void onPacket(WrappedInFlyingPacket packet) {
        if(packet.isLook()) {
            boolean cinematic = data.playerInfo.cinematicModePitch || data.playerInfo.cinematicModeYaw;
            boolean goodToGo = data.moveProcessor.yawGcdList.size() > 30;

            if(!cinematic && goodToGo) {
                long deltaX = MathUtils.getDelta(data.moveProcessor.deltaX, data.moveProcessor.lastDeltaX);
                if(data.playerInfo.lastAttack.hasNotPassed(25)
                        && data.playerInfo.lastVelocity.hasNotPassed(200)
                        && data.moveProcessor.deltaX > 0
                        && (MathUtils.getDelta(data.moveProcessor.deltaX, data.moveProcessor.lastDeltaX) < 12
                        || data.playerInfo.lastAttack.hasNotPassed(3))
                        && (data.moveProcessor.deltaY == 0 || deltaX < 3)) {
                    verbose++;
                    if(verbose > 5) {
                        vl++;
                        flag("y=" + data.moveProcessor.deltaY
                                + " sens=" + data.moveProcessor.sensitivityX
                                + " deltaX=" + deltaX
                                + " verbose=" + verbose);
                    }
                } else verbose = 0;
                debug(data.moveProcessor.deltaY + ", " + data.moveProcessor.deltaX + " verbose=" + verbose);
            }
        }
    }
}
