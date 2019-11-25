package dev.brighten.anticheat.check.impl.movement.fly;

import cc.funkemunky.api.utils.BlockUtils;
import cc.funkemunky.api.utils.BoundingBox;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.Tuple;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.CheckType;
import dev.brighten.anticheat.check.api.Event;
import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@CheckInfo(name = "Fly (F)", description = "Checks for large movement deltas", checkType = CheckType.FLIGHT,
        developer = true)
public class FlyF extends Check {

    private float lDeltaY;

    @Event
    public void onEvent(PlayerMoveEvent event) {
        if(event.getTo().distance(event.getFrom()) > 0) {
            float deltaY = (float) (event.getTo().getY() - event.getFrom().getY());
            float predicted = (lDeltaY - 0.08f) * 0.98f;

            if(lDeltaY <= 0 && MathUtils.getDelta(data.playerInfo.jumpHeight, deltaY) < 0.01f) {
                predicted = deltaY;
            }

            if(deltaY <= 0 && MathUtils.getDelta(predicted, deltaY) > 0.02f) {
                BoundingBox playerBox = new BoundingBox(event.getTo().toVector(), event.getTo().toVector())
                        .grow(0.3f,0,0.3f)
                        .add(0,0,0,0,1.8f,0);

                List<Tuple<Block, BoundingBox>> blocks = playerBox
                        .subtract(0, Math.abs(deltaY) + 0.1f,0,0,1.2f,0)
                        .getCollidingBlocks(event.getTo().getWorld()).stream()
                        .filter(tup -> BlockUtils.isSolid(tup.one)).collect(Collectors.toList());

                Tuple<Block, BoundingBox> block = blocks.stream()
                        .min(Comparator.comparing(tup ->
                                MathUtils.getDelta(tup.two.maxY - event.getFrom().getY(), deltaY)))
                        .orElse(null);

                if(block != null) {
                    predicted = (float) (block.two.maxY - event.getFrom().getY());
                }
            }

            if((data.playerInfo.blocksAboveTicks == 0 || data.playerInfo.deltaY > 0)
                    && !data.playerInfo.flightCancel
                    && !data.playerInfo.serverGround
                    && MathUtils.getDelta(predicted, deltaY) > 0.04) {
                vl++;
                flag("deltaY=" + deltaY + " predicted=" + predicted);
            } else vl-= vl > 0 ? 0.02 : 0;

            debug("deltaY= " + deltaY + " predicted=" + predicted);

            lDeltaY = deltaY;
        }
    }
}
