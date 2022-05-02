package dev.brighten.anticheat.check.impl.world.block;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockPlacePacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.KLocation;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Block (B)", description = "Looks for weird block placement", checkType = CheckType.BLOCK,
        punishVL = 6, executable = true)
@Cancellable(cancelType = CancelType.PLACE)
/* Taken from FireFlyX scaffold:b */
public class BlockB extends Check {

    private long lastPlace;
    private boolean place;
    private float buffer;

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timestamp) {
        if(data.playerInfo.creative || data.excuseNextFlying) return;

        if(place) {
            long delta = timestamp - lastPlace;
            if(delta >= 25) {
                if(++buffer >= 10f) {
                    vl++;
                    flag("");
                }
            } else if(vl > 0) vl-= 0.25f;
            place = false;
        }
    }

    @Packet
    public void onBlockPlace(WrappedInBlockPlacePacket packet, long timestamp) {
        if(data.pastLocations.isEmpty()) return;

        KLocation lastMovePacket = data.pastLocations.getLast().one;

        if(lastMovePacket == null) return;

        final long delta = timestamp - lastMovePacket.timeStamp;

        if(delta <= 25) {
            lastPlace = timestamp;
            place = true;
        } else if(buffer > 0) buffer-= 0.25f;

    }
}
