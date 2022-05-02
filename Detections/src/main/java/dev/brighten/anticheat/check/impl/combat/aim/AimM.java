package dev.brighten.anticheat.check.impl.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.EntityLocation;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Aim (M)", description = "Aim snapping", checkType = CheckType.AIM, executable = true,
        punishVL = 25)
public class AimM extends Check {

    private float buffer;
    private double ldeltaY;
    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(!packet.isLook()) return;

        double deltaXY = Math.hypot(data.moveProcessor.deltaX, data.moveProcessor.deltaY);

        check: {
            if(data.playerInfo.lastAttack.isPassed(30L)
                    || data.target == null) break check;

            EntityLocation eloc = data.entityLocationProcessor.getEntityLocation(data.target).orElse(null);

            if(eloc == null) return;

            KLocation origin = data.playerInfo.to.clone(),
                    targetLocation = new KLocation(eloc.x, eloc.y, eloc.z, eloc.yaw, eloc.pitch);


            double[] offset = MathUtils.getOffsetFromLocation(origin.toLocation(data.getPlayer().getWorld()),
                    targetLocation.toLocation(data.getPlayer().getWorld()));
            if(Math.abs(offset[0]) > 30 || Math.abs(offset[1]) > 60) break check;

            double accel = Math.abs(ldeltaY - deltaXY);

            if(accel > 200) {
                buffer++;
            } else if(accel > 150) {
                buffer+=0.5f;
            } else if(buffer > 0) buffer-= 0.2f;

            if(buffer > 4) {
                vl++;
                flag("d=%.4f p=%.4f b=%s yo=%.1f po=%.1f", accel, deltaXY, buffer, offset[0], offset[1]);
            }

            debug("d=%.4f p=%.4f b=%s yo=%.1f po=%.1f", accel, deltaXY, buffer, offset[0], offset[1]);

        }
        
        ldeltaY = deltaXY;
    }
}
