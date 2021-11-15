package dev.brighten.anticheat.check.impl.premium;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.KauriVersion;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;
import lombok.Setter;

import java.util.List;

@CheckInfo(name = "Aim (G)", description = "Statistical aim analysis",
        checkType = CheckType.AIM, planVersion = KauriVersion.ARA, devStage = DevStage.CANARY)
public class AimG extends Check {

    @Setter
    private KLocation targetLocation;

    public int streak;
    public boolean sentTeleport;

    private int abuffer;

    protected List<Double> yawOffsets = new EvictingList<>(10), pitchOffsets = new EvictingList<>(10);

    @Packet
    public void onUse(WrappedInUseEntityPacket packet) {
        if(packet.getAction() != WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK
                || !sentTeleport
                || targetLocation == null || streak < 3) return;

        KLocation origin = data.playerInfo.to.clone();

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

        find(AimJ.class).runCheck(std, pstd, offset, rot);
        find(AimK.class).runCheck(std, pstd, offset, rot);
        find(AimL.class).runCheck(std, pstd, offset, rot);
    }
}
