package dev.brighten.anticheat.check.impl.movement.fly;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.Materials;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

import java.util.ArrayList;
import java.util.List;

@CheckInfo(name = "Fly (F)", description = "Checks for liquid walk modules.", checkType = CheckType.FLIGHT,
        punishVL = 20, developer = true)
public class FlyF extends Check {

    private long aboveTicks;
    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {

        List<String> tags = new ArrayList<>();

        boolean inLiquid = data.blockInfo.inLiquid, flagged = false;

        data.blockInfo.handler.setSize(0.3, 1.8f);
        data.blockInfo.handler.setSingle(true);
        if(!inLiquid) {
            data.blockInfo.handler.setOffset(-.1);
            inLiquid = data.blockInfo.handler.isCollidedWith(Materials.WATER);
            tags.add("above");
            aboveTicks++;
            data.blockInfo.handler.setOffset(0);
        } else {
            aboveTicks = 0;
            tags.add("colliding");
        }
        data.blockInfo.handler.setSingle(false);
        data.blockInfo.handler.setSize(0.6, 1.8);

        if(inLiquid) {
            float absY = Math.abs(data.playerInfo.deltaY), labsY = Math.abs(data.playerInfo.lDeltaY);
            if(absY < 0.001) {
                vl++;
                tags.add("low");
                flagged = true;
            }
            if(absY == labsY) {
                tags.add("equal");
                vl++;
                flagged = true;
            }
            if(aboveTicks > 5) {
                vl++;
                tags.add("aboveTicks");
                flagged = true;
            }
            if(data.playerInfo.deltaY >= 0 && aboveTicks > 0) {
                vl++;
                tags.add("hover");
                flagged = true;
            }

            if(flagged) {
                if(vl > 7) {
                    flag("y=" + data.playerInfo.deltaY + " tags=" + tags);
                }
            } else vl-= vl > 0 ? 0.25f : 0;

            String joined = String.join(",", tags);
            debug("y=" + data.playerInfo.deltaY + "vl= " + vl + " tags=" + tags);
        }
    }
}
