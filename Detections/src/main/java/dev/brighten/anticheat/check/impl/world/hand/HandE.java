package dev.brighten.anticheat.check.impl.world.hand;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import org.bukkit.entity.Player;

@CheckInfo(name = "Hand (E)", description = "Incorrect interaction", checkType = CheckType.HAND, punishVL = 0)
public class HandE extends Check {

    @Packet
    public void onUse(WrappedInUseEntityPacket packet) {
        if(!(packet.getEntity() instanceof Player) || packet.getVec() == null) return;
        double x = Math.abs(packet.getVec().a), y = Math.abs(packet.getVec().b), z = Math.abs(packet.getVec().c);

        debug("x=%.2f y=%.2f z=%.2f", x, y, z);
        if(packet.getVec().a == -1 && packet.getVec().b == -1 && packet.getVec().c == -1) return;

        if(x > 0.4001 || y > 1.901 || z > 0.4001) {
            vl++;
            flag("size too large");
        }
    }
}
