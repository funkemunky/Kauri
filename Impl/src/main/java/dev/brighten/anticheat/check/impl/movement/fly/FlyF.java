package dev.brighten.anticheat.check.impl.movement.fly;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.Materials;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.TickTimer;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.Helper;
import dev.brighten.anticheat.utils.MovementUtils;
import dev.brighten.anticheat.utils.handlers.PlayerSizeHandler;
import dev.brighten.api.check.CheckType;
import lombok.val;
import org.bukkit.block.Block;

import java.util.List;

@CheckInfo(name = "Fly (F)", description = "Sets the maximum vertical speed limit.",
        checkType = CheckType.FLIGHT, punishVL = 50, developer = true)
public class FlyF extends Check {

    private float totalDeltaY, groundY;
    private int addTicks;
    private TickTimer lastSub = new TickTimer(10);
    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(!packet.isPos() || data.playerInfo.flightCancel) return;

        data.blockInfo.handler.setSize(0.8f, 2.0f);

        val box = PlayerSizeHandler.instance.bounds(data.getPlayer(),
                data.playerInfo.to.x, data.playerInfo.to.y, data.playerInfo.to.z)
                .expand(0.05f,0.05f,0.05f);
        List<Block> blocks = Helper.blockCollisions(data.blockInfo.handler.getBlocks(), box, Materials.SOLID);
        List<SimpleCollisionBox> downCasted = Helper.toCollisionsDowncasted(blocks);

        float maxHeight = data.playerInfo.jumpHeight;

        if(!data.playerInfo.clientGround) {
           totalDeltaY+= data.playerInfo.deltaY;
           addTicks++;

           if(data.playerInfo.deltaY < 0) lastSub.reset();
        }

        for(SimpleCollisionBox cbox : downCasted) {
            float delta = (float)(cbox.yMax - groundY);
            if(delta < 1) {
                maxHeight = Math.max(maxHeight, (float)(cbox.yMax - data.playerInfo.from.y));
            }

            if(delta >= 1 && MathUtils.getDelta(totalDeltaY, delta) < 1E-5 && totalDeltaY > 0) {
                vl++;
                flag("totalY=" + totalDeltaY + " height=" + delta);
                if(data.playerInfo.clientGround) {
                    totalDeltaY = 0;
                    groundY = (float)data.playerInfo.to.y;
                }
                return;
            }
        }

        float totalHeight = MovementUtils.getTotalHeight(data.getPlayer(), maxHeight);
        if(data.playerInfo.deltaY > maxHeight) {
            vl++;
            flag("deltaY=" + data.playerInfo.deltaY + " maxHeight=" + maxHeight);
        } else if(totalDeltaY > totalHeight
                || (totalDeltaY > maxHeight
                && totalDeltaY - MovementUtils.getTotalHeight(data.getPlayer(), maxHeight) > 0.1f)) {
            vl++;
            flag("total=" + totalDeltaY + " predicted=" + totalHeight);
        }

        debug("deltaY=" + data.playerInfo.deltaY + " total=" + totalDeltaY
                + " pred=" + totalHeight + " maxOne=" + maxHeight + " size=" + downCasted.size() + ", " + blocks.size());
        if(data.playerInfo.clientGround) {
            totalDeltaY = 0;
            addTicks = 0;
            groundY = (float) data.playerInfo.to.y;
        }
    }
}
