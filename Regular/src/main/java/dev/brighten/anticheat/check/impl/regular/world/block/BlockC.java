package dev.brighten.anticheat.check.impl.regular.world.block;

import cc.funkemunky.api.utils.math.cond.MaxDouble;
import cc.funkemunky.api.utils.world.types.RayCollision;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;
import org.bukkit.Location;
import org.bukkit.event.block.BlockPlaceEvent;

@CheckInfo(name = "Block (C)", description = "Checks if a player places a block without looking.",
        checkType = CheckType.BLOCK, vlToFlag = 3, punishVL = 9, devStage = DevStage.BETA, executable = true)
@Cancellable(cancelType = CancelType.INTERACT)
public class BlockC extends Check {

    private final MaxDouble verbose = new MaxDouble(20);

    @Event
    public void onBlockPlace(BlockPlaceEvent event) {
        final Location loc = event.getBlockAgainst().getLocation();
        final SimpleCollisionBox box = new SimpleCollisionBox(loc, 2, 1).expand(0.15, 0.15, 0.15);

        final Location origin = event.getPlayer().getEyeLocation();
        final RayCollision collision = new RayCollision(origin.toVector(), origin.getDirection());
        final boolean collided = collision.isCollided(box);

        if(!collided) {
            if(verbose.add() > 4) {
                vl++;
                flag("to=[x=%.1f y=%.1f z=%.1f yaw=%.1f pitch=%.1f] loc=[%.1f,%.1f,%.1f]",
                        origin.getX(), origin.getY(), origin.getZ(), origin.getYaw(), origin.getPitch(),
                        loc.getX(), loc.getY(), loc.getZ());
            }
        } else verbose.subtract(0.5);

        debug("collided=%s verbose=%s", collided, verbose.value());
    }
}
