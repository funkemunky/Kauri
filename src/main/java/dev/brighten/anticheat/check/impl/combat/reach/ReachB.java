package dev.brighten.anticheat.check.impl.combat.reach;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.CheckType;
import dev.brighten.anticheat.check.api.Packet;
import org.bukkit.Location;

import java.util.List;
import java.util.stream.Collectors;

@CheckInfo(name = "Reach (B)", developer = true, checkType = CheckType.HITBOX)
public class ReachB extends Check {

    @Packet
    public void onUse(WrappedInUseEntityPacket packet) {
        List<Location> origin = data.pastLocation
                .getEstimatedLocation(MathUtils.floor(data.lagInfo.transPing / 2f), 100)
                .stream()
                .map(kloc -> kloc.toLocation(data.getPlayer().getWorld()))
                .collect(Collectors.toList());
        List<Location> entityLoc = data.targetPastLocation
                .getEstimatedLocation(MathUtils.floor(data.lagInfo.transPing / 2f), 100L)
                .stream()
                .map(kloc -> kloc.toLocation(data.getPlayer().getWorld()))
                .collect(Collectors.toList());

        if(data.lagInfo.lastPacketDrop.hasNotPassed(1)) debug("lagged");

        double distance = origin.stream().mapToDouble(loc ->   entityLoc
                .stream()
                .mapToDouble(kloc -> MathUtils.getHorizontalDistance(kloc, loc) - 0.3)
                .summaryStatistics().getMin())
                .summaryStatistics().getMin();

        debug("distance=" + distance);
    }
}
