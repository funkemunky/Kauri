package dev.brighten.anticheat.check.impl.world.hand;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.com.github.retrooper.packetevents.util.Vector3f;
import cc.funkemunky.api.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

@CheckInfo(name = "Hand (E)", description = "Incorrect interaction", checkType = CheckType.HAND, punishVL = 0)
public class HandE extends Check {

    @Packet
    public void onUse(WrapperPlayClientInteractEntity packet) {
        Entity entity =  Atlas.getInstance().getWorldInfo(data.getPlayer().getWorld())
                .getEntityOrLock(packet.getEntityId()).orElse(null);

        if(!(entity instanceof Player) || !packet.getTarget().isPresent()) return;

        Vector3f target = packet.getTarget().get();

        double x = Math.abs(target.x), y = Math.abs(target.y), z = Math.abs(target.z);

        debug("x=%.2f y=%.2f z=%.2f", x, y, z);
        if(target.x == -1 && target.y == -1 && target.z == -1) return;

        if(x > 0.4001 || y > 1.901 || z > 0.4001) {
            vl++;
            flag("size too large");
        }
    }
}
