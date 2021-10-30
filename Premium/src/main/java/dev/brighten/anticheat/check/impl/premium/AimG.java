package dev.brighten.anticheat.check.impl.premium;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.KauriVersion;
import dev.brighten.api.check.CheckType;;import java.util.List;

@CheckInfo(name = "Aim (G)", description = "gcd fix patching",
        checkType = CheckType.AIM, punishVL = 30, developer = true, planVersion = KauriVersion.ARA)
public class AimG extends Check {

    private int buffer;
    private double lldp;
    private List<Double> errors = new EvictingList<>(40);
    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(!packet.isLook()) return;

        double dp = Math.abs(data.playerInfo.deltaPitch), ldp = Math.abs(data.playerInfo.lDeltaPitch);

        if(dp == 0 && ldp > 0 && lldp > 0 && ldp == lldp)
            debug("&aFlags");

        lldp= ldp;
    }
}