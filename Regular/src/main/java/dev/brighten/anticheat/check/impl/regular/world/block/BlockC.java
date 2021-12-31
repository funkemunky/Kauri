package dev.brighten.anticheat.check.impl.regular.world.block;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockPlacePacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.math.IntVector;
import cc.funkemunky.api.utils.math.cond.MaxDouble;
import cc.funkemunky.api.utils.world.types.RayCollision;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;
import org.bukkit.Location;

@CheckInfo(name = "Block (C)", description = "Checks if a player places a block without looking.",
        checkType = CheckType.BLOCK, vlToFlag = 3, punishVL = 15, devStage = DevStage.BETA, executable = true)
@Cancellable(cancelType = CancelType.INTERACT)
public class BlockC extends Check {

    private final MaxDouble verbose = new MaxDouble(20);
    private SimpleCollisionBox box;
    private Location loc;

    @Packet
    public void onPlace(WrappedInBlockPlacePacket packet) {
        IntVector pos = packet.getBlockPosition();
        if(pos != null && (pos.getX() != -1 || (pos.getY() != -1 && pos.getY() != 255) || pos.getZ() != -1)) {
            loc = new Location(packet.getPlayer().getWorld(), pos.getX(), pos.getY(), pos.getZ());

            this.box = new SimpleCollisionBox(loc, 2, 1).expand(0.15, 0.15, 0.15);
        }
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(box != null && loc != null && data.playerInfo.lastFlyingTimer.isPassed(1)) {
            KLocation origin = data.playerInfo.to.clone();

            origin.y+= data.playerInfo.sneaking ? 1.54 : 1.62;

            RayCollision collision = new RayCollision(origin.toVector(), MathUtils.getDirection(origin));

            boolean collided = collision.isCollided(box);
            if(!collided) {
                if(verbose.add() > 4) {
                    vl++;
                    flag("to=[x=%.1f y=%.1f z=%.1f yaw=%.1f pitch=%.1f] loc=[%.1f,%.1f,%.1f]",
                            origin.x, origin.y, origin.z, origin.yaw, origin.pitch,
                            loc.getX(), loc.getY(), loc.getZ());
                }
            } else verbose.subtract(0.5);

            debug("collided=%s verbose=%s", collided, verbose.value());
            box = null;
            loc = null;
        }
    }
}
