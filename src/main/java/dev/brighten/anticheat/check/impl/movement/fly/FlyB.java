package dev.brighten.anticheat.check.impl.movement.fly;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.KLocation;
import org.bukkit.util.Vector;

@CheckInfo(name = "FlyB")
public class FlyB extends Check {

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(packet.isPos()
                && (data.playerInfo.deltaX != 0 || data.playerInfo.deltaY != 0 || data.playerInfo.deltaZ != 0)) {
            Vector to = data.playerInfo.to.toVector();
            Vector predicted = new Vector(data.predictionService.posX, data.predictionService.posY, data.predictionService.posZ);

            debug("distance=" + to.distance(predicted));

            /*debug("pos=(" + to.getX() + ", " + to.getY() + ", " + to.getZ() + "), " +
                    "predicted=(" + data.predictionService.posX + ", "
                    + data.predictionService.posY + ", "
                    + data.predictionService.posZ);*/
        }
    }
}
