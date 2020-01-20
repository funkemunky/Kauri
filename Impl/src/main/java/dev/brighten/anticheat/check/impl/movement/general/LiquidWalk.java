package dev.brighten.anticheat.check.impl.movement.general;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.Materials;
import cc.funkemunky.api.utils.PlayerUtils;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.MovementUtils;
import dev.brighten.api.check.CheckType;
import lombok.val;
import lombok.var;

import java.util.ArrayList;
import java.util.List;

@CheckInfo(name = "LiquidWalk", description = "Checks for liquid walk modules.", checkType = CheckType.GENERAL,
        punishVL = 20, developer = true)
@Cancellable
public class LiquidWalk extends Check {

    private long aboveTicks;
    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(data.playerInfo.generalCancel) return;
        List<String> tags = new ArrayList<>();

        boolean inLiquid = data.blockInfo.inLiquid, flagged = false;

        data.blockInfo.handler.setSize(0.3, 1.8f);
        data.blockInfo.handler.setSingle(true);
        if(!inLiquid) {
            if(!data.playerInfo.serverGround) {
                data.blockInfo.handler.setOffset(-.1);
                inLiquid = data.blockInfo.handler.isCollidedWith(Materials.WATER);
                tags.add("above");
                aboveTicks++;
                data.blockInfo.handler.setOffset(0);
            }
        } else {
            aboveTicks = 0;
            tags.add("colliding");
        }
        data.blockInfo.handler.setSingle(false);
        data.blockInfo.handler.setSize(0.6, 1.8);

        if(inLiquid) {
            double absY = Math.abs(data.playerInfo.deltaY), labsY = Math.abs(data.playerInfo.lDeltaY);
            if(absY == labsY && !data.playerInfo.clientGround && !data.playerInfo.serverGround && data.playerInfo.deltaY > -0.05)
                tags.add("equal");

            if(aboveTicks > 5) tags.add("aboveTicks");

            if(data.playerInfo.deltaY >= 0 && aboveTicks > 0) tags.add("hover");

            var base = MovementUtils.getBaseSpeed(data) - 0.04f;

            base+= 0.1f * PlayerUtils.getDepthStriderLevel(data.getPlayer());
            if(data.playerInfo.deltaXZ > base && data.playerInfo.liquidTicks.value() > 8) {
                tags.add("speed[" + data.playerInfo.deltaXZ + ">-" + base + "]");
            }

            String joined = String.join(",", tags);
            if(tags.size() > 1) {
                if(vl++ > 7) {
                    flag("y=%1 tags=[%2]", data.playerInfo.deltaY, joined);
                }
            } else vl-= vl > 0 ? 0.25f : 0;
            debug("y=" + data.playerInfo.deltaY + "vl= " + vl + " tags=" + joined);
        }
    }
}
