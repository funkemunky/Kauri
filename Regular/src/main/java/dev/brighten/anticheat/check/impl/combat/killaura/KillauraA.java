package dev.brighten.anticheat.check.impl.combat.killaura;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.math.cond.MaxInteger;
import cc.funkemunky.api.utils.world.EntityData;
import cc.funkemunky.api.utils.world.types.RayCollision;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.Optional;

@CheckInfo(name = "Killaura (A)", description = "Checks for block collisions on player hits.",
        checkType = CheckType.KILLAURA, developer = true)
@Cancellable(cancelType = CancelType.ATTACK)
public class KillauraA extends Check {

    private MaxInteger verbose = new MaxInteger(10);

    private int lastAttack;

    @Packet
    public void onUse(WrappedInUseEntityPacket packet, int current) {
        if(data.target == null || packet.getAction() != WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK) return;

        lastAttack = current;
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, int current) {
        if(current - lastAttack == 0) {
            Location toLoc = data.playerInfo.to.toLocation(data.getPlayer().getWorld())
                    .add(0, data.playerInfo.sneaking ? 1.54f : 1.62f, 0);

            RayCollision from = new RayCollision(data.getPlayer().getEyeLocation().toVector(),
                    data.getPlayer().getEyeLocation().getDirection()),
                    to = new RayCollision(toLoc.toVector(), toLoc.getDirection());

            Vector sixNineVector = toLoc.toVector().subtract(new Vector(69, 69, 69));

            double distance = data.targetPastLocation.getEstimatedLocation(System.currentTimeMillis(),
                    (data.lagInfo.transPing + 3) * 50, 100L).stream().mapToDouble(loc -> {
                SimpleCollisionBox box = (SimpleCollisionBox) EntityData.getEntityBox(loc, data.target);

                return Math.min(Optional.ofNullable(from.collisionPoint(box)).orElse(sixNineVector)
                        .distance(data.getPlayer().getEyeLocation().toVector()),
                        Optional.ofNullable(to.collisionPoint(box)).orElse(sixNineVector)
                                .distance(toLoc.toVector()));
                    }).min().orElse(0);


            int fromBox = from.boxesOnRay(data.getPlayer().getWorld(), distance).size()
                    , toBox = to.boxesOnRay(data.getPlayer().getWorld(), distance).size();

            if(fromBox > 0 && toBox > 0) {
                debug(Color.Green + "Flag");
            }
        }
    }
}
