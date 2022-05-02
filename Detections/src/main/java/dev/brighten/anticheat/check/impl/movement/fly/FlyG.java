package dev.brighten.anticheat.check.impl.movement.fly;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.processing.TagsBuilder;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;

@CheckInfo(name = "Fly (G)", description = "Looks for impossible movements, commonly done by Step modules",
        devStage = DevStage.ALPHA, checkType = CheckType.FLIGHT, punishVL = 12)
@Cancellable
public class FlyG extends Check {

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(!packet.isPos() || data.playerInfo.doingBlockUpdate) return;

        boolean toGround = data.playerInfo.clientGround && data.playerInfo.serverGround;
        boolean fromGround = data.playerInfo.lClientGround && data.playerInfo.lServerGround;

        TagsBuilder tags = new TagsBuilder();

        double max = data.playerInfo.jumpHeight;
        if(toGround) {
            if(!fromGround) {
                if(data.playerInfo.lDeltaY > 0 && data.playerInfo.lastFenceBelow.isPassed(4)
                        && data.playerInfo.blockAboveTimer.isPassed(2)) {
                    tags.addTag("INVALID_LANDING");
                    max = 0;
                }
            } else {
                if(data.blockInfo.onSlab || data.blockInfo.onStairs)
                    max = 0.5;
                else if(data.blockInfo.onHalfBlock || data.blockInfo.miscNear)
                    max = 0.5625;

                tags.addTag("GROUND_STEP");
                tags.addTag("max=" + max);
            }
        }

        if(data.playerInfo.deltaY > max && tags.getSize() > 0 && !data.playerInfo.flightCancel) {
            vl++;
            flag("t=" + tags.build());
        }
    }
}
