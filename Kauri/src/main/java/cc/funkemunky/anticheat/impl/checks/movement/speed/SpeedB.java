package cc.funkemunky.anticheat.impl.checks.movement.speed;

import cc.funkemunky.anticheat.api.checks.*;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import lombok.val;
import org.bukkit.event.Event;

@CheckInfo(name = "Speed (Type E)", type = CheckType.SPEED, cancelType = CancelType.MOTION, maxVL = 40)
@Init
@Packets(packets = {Packet.Client.POSITION, Packet.Client.POSITION_LOOK, Packet.Client.LEGACY_POSITION_LOOK, Packet.Client.LEGACY_POSITION})
public class SpeedB extends Check {

    private float vl = 0;
    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(getData().isGeneralCancel()) return;
        val move = getData().getMovementProcessor();
        float threshold = MiscUtils.getBaseSpeed(getData()) + (move.isServerOnGround() ? 0.07f : 0.09f);

        threshold+= move.getHalfBlockTicks() > 0 ? 0.08 : 0;
        threshold+= move.isOnSlimeBefore() ? 0.025 : 0;
        threshold+= move.getBlockAboveTicks() > 0 ? (move.getIceTicks() > 0 ? 0.565f : 0.25f) : 0;
        threshold+= move.getIceTicks() > 0 && move.getGroundTicks() < 8 ? 0.15 : 0;

        if(move.getDeltaXZ() > threshold) {
            if(Math.min(vl+= 2, 40) > 30) {
                flag("speed=" + move.getDeltaXZ(), true, true, AlertTier.HIGH);
            }
        } else vl-= vl > 0 ? 1 : 0;

        debug(move.getDeltaXZ() + ">-" + threshold + " vl=" + vl);
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
