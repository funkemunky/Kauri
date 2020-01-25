package dev.brighten.anticheat.check.impl.movement.fly;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.world.BlockData;
import cc.funkemunky.api.utils.world.CollisionBox;
import cc.funkemunky.api.utils.world.types.ComplexCollisionBox;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import com.sun.xml.internal.ws.resources.UtilMessages;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.Helper;
import dev.brighten.api.check.CheckType;
import io.netty.util.internal.MathUtil;
import lombok.val;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@CheckInfo(name = "Fly (F)", description = "Flight prediction check.", checkType = CheckType.FLIGHT, developer = true)
@Cancellable
public class FlyF extends Check {

    private double predictedY;
    private static ProtocolVersion gameVersion = ProtocolVersion.getGameVersion();
    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(packet.isPos() && (data.playerInfo.deltaXZ > 0 || data.playerInfo.deltaY != 0)) {
            if(data.playerInfo.jumped) {
                predictedY = Math.min(data.playerInfo.deltaY, (double) data.playerInfo.jumpHeight);
            } else {
                predictedY-= 0.08;
                predictedY*= 0.9800000190734863D;
            }

            if(Math.abs(predictedY) < 0.005) {
                predictedY = 0;
                if(Math.abs(predictedY) < 1E-4 && data.playerInfo.deltaXZ == 0 && !data.playerInfo.clientGround) {
                    predictedY-= 0.08;
                    predictedY*= 0.9800000190734863D;
                }
            }

            if(data.blockInfo.collidesVertically) {
                val list = data.blockInfo.verticalCollisions.stream()
                        .map(block -> BlockData.getData(block.getType()).getBox(block, gameVersion))
                        .collect(Collectors.toList());

                for (CollisionBox cbox : list) {
                    List<SimpleCollisionBox> boxes = new ArrayList<>();

                    cbox.downCast(boxes);

                    for (SimpleCollisionBox box : boxes) {
                        double yOne = (box.yMin - (data.playerInfo.from.y + 1.8));
                        double yTwo = (box.yMax - data.playerInfo.from.y);

                        if(!data.playerInfo.clientGround) debug(Color.Red + "one=%1 two=%2", yOne, yTwo);
                        if(MathUtils.getDelta(yOne, data.playerInfo.deltaY) < 1E-6) {
                            predictedY = yOne;
                            break;
                        } else if(MathUtils.getDelta(yTwo, data.playerInfo.deltaY) < 1E-6) {
                            predictedY = yTwo;
                            break;
                        }
                    }
                }
            }

            if(MathUtils.getDelta(data.playerInfo.deltaY, predictedY) > 1E-5) {
                double lpredicted = predictedY;
                predictedY = 0;
                predictedY-= 0.08;
                predictedY*= 0.9800000190734863D;
                if(MathUtils.getDelta(data.playerInfo.deltaY, predictedY) > 1E-5) predictedY = lpredicted;
            }

            if(data.playerInfo.wasOnSlime) predictedY = data.playerInfo.deltaY;

            if(MathUtils.getDelta(data.playerInfo.deltaY, predictedY) > 1E-5
                    && !data.playerInfo.flightCancel) {
                vl++;
                flag("deltaY=%1 predY=%2", data.playerInfo.deltaY, predictedY);
            }

            debug("deltaY=" + data.playerInfo.deltaY + " pred=" + predictedY
                    + " collide=" + data.blockInfo.collidesVertically + " ground=" + data.playerInfo.clientGround);
        } else predictedY = 0;
    }
}
