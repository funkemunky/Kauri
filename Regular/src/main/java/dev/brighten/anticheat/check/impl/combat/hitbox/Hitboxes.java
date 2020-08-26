package dev.brighten.anticheat.check.impl.combat.hitbox;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.world.EntityData;
import cc.funkemunky.api.utils.world.types.RayCollision;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.api.check.CheckType;
import lombok.val;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.stream.Collectors;

@CheckInfo(name = "Hitboxes", description = "Checks if the player attacks outside a player's hitbox.",
        checkType = CheckType.HITBOX, punishVL = 15, developer = true)
@Cancellable(cancelType = CancelType.ATTACK)
public class Hitboxes extends Check {

    private boolean useEntity;
    private float buffer;
    private Entity target;

    @Packet
    public void onUse(WrappedInUseEntityPacket packet) {
        if (packet.getAction().equals(WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK)) {
            useEntity = true;
            target = packet.getEntity();
        }
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long now) {
        if (useEntity) {
            check:
            {
                if(!target.getLocation().getWorld().getUID()
                        .equals(packet.getPlayer().getLocation().getWorld().getUID()))
                    break check;

                if(data.pastLocation.previousLocations.size() < 10
                        || data.getPlayer().getGameMode().equals(GameMode.CREATIVE)
                        || data.getPlayer().getGameMode().equals(GameMode.SPECTATOR)) break check;

                double distance = target.getLocation().distance(packet.getPlayer().getLocation());
                double vExpand = Math.abs(data.playerInfo.deltaPitch / 90) * distance;
                double hExpand = Math.abs(data.playerInfo.deltaYaw / 180) * distance;

                Location origin = packet.getPlayer().getLocation().clone()
                        .add(0, packet.getPlayer().isSneaking() ? 1.54 : 1.62, 0);

                RayCollision ray = new RayCollision(origin.toVector(), origin.getDirection());
                boolean collided = data.targetPastLocation.getEstimatedLocation(now, data.lagInfo.transPing, 150L)
                        .stream().map(loc -> ((SimpleCollisionBox)EntityData.getEntityBox(loc, target))
                                .expand(0.1).expand(hExpand, vExpand, hExpand))
                        .anyMatch(ray::isCollided);

                if(!collided) {
                    if(++buffer > 2) {
                        vl++;
                        flag("v=%v.3 h=%v.3 ping=%v", vExpand, hExpand, data.lagInfo.transPing);
                    }
                } else if(buffer > 0) buffer-= 0.2;

                debug("buffer=%v.1 collided=%v", buffer, collided);
            }
            useEntity = false;
        }
    }

}