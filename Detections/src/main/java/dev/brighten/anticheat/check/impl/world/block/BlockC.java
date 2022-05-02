package dev.brighten.anticheat.check.impl.world.block;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockPlacePacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.types.enums.WrappedEnumParticle;
import cc.funkemunky.api.utils.BlockUtils;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.Tuple;
import cc.funkemunky.api.utils.math.IntVector;
import cc.funkemunky.api.utils.math.cond.MaxDouble;
import cc.funkemunky.api.utils.world.BlockData;
import cc.funkemunky.api.utils.world.CollisionBox;
import cc.funkemunky.api.utils.world.types.RayCollision;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;
import lombok.val;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.Collections;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

@CheckInfo(name = "Block (C)", description = "Checks if a player places a block without looking.",
        checkType = CheckType.BLOCK, vlToFlag = 3, punishVL = 9, devStage = DevStage.BETA, executable = true)
@Cancellable(cancelType = CancelType.INTERACT)
public class BlockC extends Check {

    private final MaxDouble verbose = new MaxDouble(20);
    private Queue<Tuple<Block, SimpleCollisionBox>> blockPlacements = new LinkedBlockingQueue<>();

    @Packet
    public void onBlockPlace(WrappedInBlockPlacePacket event) {
        Location loc = event.getBlockPosition().toBukkitVector().toLocation(event.getPlayer().getWorld());
        Optional<Block> optionalBlock = BlockUtils.getBlockAsync(loc);

        if(!optionalBlock.isPresent()) return;

        final Block block = optionalBlock.get();
        CollisionBox box = BlockData.getData(block.getType()).getBox(block, data.playerVersion);

        if(!(box instanceof SimpleCollisionBox)) {
            debug("Not SimpleCollisionBox");
            return;
        }

        final SimpleCollisionBox simpleBox = ((SimpleCollisionBox) box);

        if(Math.abs(simpleBox.yMax - simpleBox.yMin) != 1.
                || Math.abs(simpleBox.xMax - simpleBox.xMin) != 1.
                || Math.abs(simpleBox.zMax - simpleBox.zMin) != 1.) {
            debug("not full block: x=%.1f y=%.1f z=%.1f",
                    Math.abs(simpleBox.xMax - simpleBox.xMin),
                    Math.abs(simpleBox.yMax - simpleBox.yMin),
                    Math.abs(simpleBox.zMax - simpleBox.zMin));
            return;
        }

        blockPlacements.add(new Tuple<>(block, simpleBox.expand(0.1)));
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        Tuple<Block, SimpleCollisionBox> tuple;

        while((tuple = blockPlacements.poll()) != null) {
            final SimpleCollisionBox box = tuple.two;
            final Block block = tuple.one;

            final KLocation to = data.playerInfo.to.clone(), from = data.playerInfo.from.clone();

            to.y+= data.playerInfo.sneaking ? (ProtocolVersion.getGameVersion().isOrAbove(ProtocolVersion.V1_14)
                    ? 1.27f : 1.54f) : 1.62f;
            from.y+= data.playerInfo.lsneaking ? (ProtocolVersion.getGameVersion().isOrAbove(ProtocolVersion.V1_14)
                    ? 1.27f : 1.54f) : 1.62f;

            final RayCollision rayTo = new RayCollision(to.toVector(),
                    MathUtils.getDirection(to)),
                    rayFrom = new RayCollision(data.playerInfo.from.toVector(),
                            MathUtils.getDirection(data.playerInfo.from));

            final boolean collided = rayTo.isCollided(box) || rayFrom.isCollided(box);

            if(!collided) {
                if(verbose.add() > 4) {
                    vl++;
                    flag("to=[x=%.1f y=%.1f z=%.1f yaw=%.1f pitch=%.1f] loc=[%.1f,%.1f,%.1f]",
                            to.x, to.y, to.z, to.yaw, from.pitch,
                            block.getLocation().getX(), block.getLocation().getY(), block.getLocation().getZ());
                }
            } else verbose.subtract(0.5);

            debug("collided=%s verbose=%s", collided, verbose.value());
        }
    }
}
