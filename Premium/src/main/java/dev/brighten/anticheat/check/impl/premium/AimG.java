package dev.brighten.anticheat.check.impl.premium;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.check.impl.premium.hitboxes.ReachB;
import dev.brighten.anticheat.check.impl.premium.util.EntityLocation;
import dev.brighten.anticheat.utils.timer.Timer;
import dev.brighten.anticheat.utils.timer.impl.TickTimer;
import dev.brighten.api.KauriVersion;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;
import lombok.Setter;

import java.util.List;

@CheckInfo(name = "Aim (G)", description = "Statistical aim analysis",
        checkType = CheckType.AIM, planVersion = KauriVersion.ARA, punishVL = 20, executable = true)
public class AimG extends Check {

    private int abuffer;

    protected List<Double> yawOffsets = new EvictingList<>(10), pitchOffsets = new EvictingList<>(10);

    @Packet
    public void onUse(WrappedInUseEntityPacket packet) {
        ReachB reach = find(ReachB.class);
        if(packet.getAction() != WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK
                || data.target == null || reach.streak < 3
                || !reach.entityLocationMap.containsKey(data.target.getUniqueId())) return;

        EntityLocation eloc = reach.entityLocationMap.get(data.target.getUniqueId());

        if(!eloc.sentTeleport) return;

        KLocation origin = data.playerInfo.to.clone(),
                targetLocation = new KLocation(eloc.x, eloc.y, eloc.z, eloc.yaw, eloc.pitch);

        origin.y+= data.playerInfo.sneaking ? 1.54f : 1.62f;

        double[] offset = MathUtils.getOffsetFromLocation(origin.toLocation(data.getPlayer().getWorld()),
                targetLocation.toLocation(data.getPlayer().getWorld()));

        float[] rot = MathUtils.getRotations(origin.toLocation(data.getPlayer().getWorld()),
                targetLocation.toLocation(data.getPlayer().getWorld()));

        typeA: {
            if(offset[0] == 0D) {
                if(data.playerInfo.deltaYaw > 0.2 && ++abuffer > 5) {
                    vl++;
                    abuffer = 5;
                    flag("t=a y=%.2f dy=%.3f", offset[1], data.playerInfo.deltaYaw);
                }
            } else abuffer = 0;
        }

        offset[0] = MathUtils.yawTo180D(offset[0]);
        yawOffsets.add(offset[0]);
        pitchOffsets.add(offset[1]);

        if(yawOffsets.size() < 8 || pitchOffsets.size() < 8) return;
        double std = MathUtils.stdev(yawOffsets);
        double pstd = MathUtils.stdev(pitchOffsets);

        debug("ys=%.3f ps=%.3f po=%.1f yo=%.1f", std, pstd, offset[1], offset[0]);

        find(AimJ.class).runCheck(std, pstd, offset, rot);
        find(AimK.class).runCheck(std, pstd, offset, rot);
        find(AimL.class).runCheck(std, pstd, offset, rot);
    }
}
