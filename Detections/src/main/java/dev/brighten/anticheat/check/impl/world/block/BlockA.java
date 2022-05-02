package dev.brighten.anticheat.check.impl.world.block;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockPlacePacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;
import org.bukkit.util.Vector;

@CheckInfo(name = "Block (A)", description = "Checks for impossible scaffold sprinting.", devStage = DevStage.RELEASE,
        checkType = CheckType.BLOCK, executable = true, punishVL = 8)
@Cancellable(cancelType = CancelType.PLACE)
public class BlockA extends Check {

    private int buffer;

    @Packet
    public void onBlock(WrappedInBlockPlacePacket event) {
        Vector dir = new Vector(event.getFace().getAdjacentX(),
                0, event.getFace().getAdjacentZ()),
                opposite = new Vector(event.getFace().opposite().getAdjacentX(),
                        0, event.getFace().opposite().getAdjacentZ());

        //Getting place block
        Vector delta = new Vector(data.playerInfo.deltaX, data.playerInfo.deltaY, data.playerInfo.deltaZ);

        double dist = delta.distance(dir), dist2 = opposite.distance(MathUtils.getDirection(data.playerInfo.to).setY(0));
        boolean check = dist <= 1 && dist > 0.7 && dist2 >= 0.5 && dist2 < 1;

        if(check && event.getFace().getAdjacentY() == 0
                && event.getPlayer().getItemInHand().getType().isBlock()
                && data.playerInfo.sprinting) {
            if((buffer+= 4) > 15) {
                vl++;
                flag("dist=%.3f dist2=%.3f placeVec=%s", dist, dist2, dir.toString());
                buffer = 14;
            }
        } else if(buffer > 0) buffer--;

        debug("dist=%.3f dist2=%.3f", dist, dist2);
    }
}
