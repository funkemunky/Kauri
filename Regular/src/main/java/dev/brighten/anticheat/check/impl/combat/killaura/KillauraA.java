package dev.brighten.anticheat.check.impl.combat.killaura;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.math.cond.MaxInteger;
import cc.funkemunky.api.utils.world.CollisionBox;
import cc.funkemunky.api.utils.world.EntityData;
import cc.funkemunky.api.utils.world.types.RayCollision;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.api.check.CheckType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@CheckInfo(name = "Killaura (A)", description = "Checks for block collisions on player hits.",
        checkType = CheckType.KILLAURA, developer = true)
@Cancellable(cancelType = CancelType.ATTACK)
public class KillauraA extends Check {

    private MaxInteger verbose = new MaxInteger(10);

    @Packet
    public void onUse(WrappedInUseEntityPacket packet, long current) {
        if(data.target == null) return;
        KLocation origin = data.playerInfo.to.clone();
        origin.y+= data.playerInfo.sneaking ? 1.54f : 1.62f;

        RayCollision ray = new RayCollision(origin.toVector(), MathUtils.getDirection(origin));

        List<SimpleCollisionBox> targetBoxes = new ArrayList<>();

        data.targetPastLocation.getEstimatedLocation(data.lagInfo.transPing + 50, 150)
                .forEach(loc -> EntityData.getEntityBox(loc, data.target).downCast(targetBoxes));

        double distance = targetBoxes.stream().map(ray::collisionPoint)
                .filter(Objects::nonNull).mapToDouble(vec -> vec.distance(origin.toVector())).min().orElse(-1);

        if(distance == -1) return;

        List<CollisionBox> boxes = ray.boxesOnRay(packet.getPlayer().getWorld(), distance);

        debug("boxSize=%v", boxes.size());
    }
}
