package dev.brighten.anticheat.check.impl.combat.hand;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockPlacePacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.BlockUtils;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.math.cond.MaxDouble;
import cc.funkemunky.api.utils.world.BlockData;
import cc.funkemunky.api.utils.world.types.RayCollision;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;
import lombok.val;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

@CheckInfo(name = "Hand (D)", description = "Checks if a player places a block without looking.",
        checkType = CheckType.HAND, vlToFlag = 3, punishVL = 40, developer = true, enabled = false)
@Cancellable(cancelType = CancelType.INTERACT)
public class HandD extends Check {

    private MaxDouble verbose = new MaxDouble(20);
    private Block block;

    @Packet
    public void onPlace(WrappedInBlockPlacePacket packet) {
        val pos = packet.getPosition();
        if(pos != null && (pos.getX() != -1 || pos.getY() != -1 || pos.getZ() != -1)) {
            Location loc = new Location(packet.getPlayer().getWorld(), pos.getX(), pos.getY(), pos.getZ());

            Block block = BlockUtils.getBlock(loc);

            if(block == null) return;

            this.block = block;
        }
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(block != null) {
            List<SimpleCollisionBox> boxes = new ArrayList<>();

            BlockData.getData(block.getType()).getBox(block, ProtocolVersion.getGameVersion())
                    .downCast(boxes);

            if(boxes.size() == 0) {
                debug("collided=false but will not flag since no collision boxes were found for block.");
                return;
            }

            val origin = data.playerInfo.to.clone();

            origin.y+= data.playerInfo.sneaking ? 1.54 : 1.62;

            RayCollision collision = new RayCollision(origin.toVector(), MathUtils.getDirection(origin));

            boolean collided = false;

            for (SimpleCollisionBox box : boxes) {
                box.expand(0.1,0.1,0.1);

                if(collision.isCollided(box)) {
                    collided = true;
                    break;
                }
            }

            if(!collided) {
                if(verbose.add() > 4) {
                    vl++;
                    flag("type=%s", block.getType().name());
                }
            } else verbose.subtract(0.5);

            debug("collided=%s verbose=%s", collided, verbose.value());
            block = null;
        }
    }
}
