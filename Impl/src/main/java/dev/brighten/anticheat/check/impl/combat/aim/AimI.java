package dev.brighten.anticheat.check.impl.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import org.bukkit.util.Vector;

@CheckInfo(name = "Aim (I)", description = "Checks for large head snaps. By Itz_Lucky",
        checkType = CheckType.AIM, punishVL = 10)
public class AimI extends Check {

    private double verbose;

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(!packet.isLook() || data.playerInfo.generalCancel) return;

        Vector vector = new Vector(data.playerInfo.deltaX, 0, data.playerInfo.deltaZ);
        double angleMove = vector.distanceSquared((
                new Vector(
                        data.playerInfo.to.yaw - data.playerInfo.from.yaw,
                        0,
                        data.playerInfo.to.yaw - data.playerInfo.from.yaw)));

        if(angleMove > 100000 && data.moveProcessor.sensitivityX < 0.4 && data.playerInfo.deltaXZ > 0.1) {
            if(verbose++ > 1) {
                vl++;
                flag("angle=%1,deltaXZ=%2", angleMove, MathUtils.round(data.playerInfo.deltaXZ, 4));
            }
        } else verbose-= verbose > 0 ? 0.005 : 0;

        debug("angle=%1 deltaXZ=%2 verbose=%3", angleMove, data.playerInfo.deltaXZ, verbose);
    }
}
