package dev.brighten.anticheat.premium.impl.hitboxes;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.RelativePastLocation;
import dev.brighten.api.check.CheckType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.Optional;

@CheckInfo(name = "Reach (C)", description = "Test reach check.", checkType = CheckType.HITBOX, developer = true)
public class ReachC extends Check {

    @Packet
    public void onUse(WrappedInUseEntityPacket packet, int now) {
        if(packet.getAction().equals(WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK)) {
            if (!(packet.getEntity() instanceof LivingEntity)) {
                return;
            }

            int tick = now - data.lagInfo.transPing - 4;

            Optional<RelativePastLocation.RelativeLocation> loc = data.relTPastLocation.getLocation(tick);

            if(!loc.isPresent()) {
                debug("Location was not present: size=%v tick=%v ping=%v",
                        data.relTPastLocation.getPastLocations().size(), tick, data.lagInfo.transPing);
                return;
            }

            RelativePastLocation.RelativeLocation location = loc.get();

            Vector currentPos = location.getPosition(3 - Math.abs(tick - location.tick));

            double distance = currentPos.clone().setY(0).distance(data.playerInfo.to.toVector().setY(0));
            debug("%v.2", distance);
            //debug("distance=%v.2 locTick=%v tick=%v", distance, location.tick, tick);
        }
    }
}
