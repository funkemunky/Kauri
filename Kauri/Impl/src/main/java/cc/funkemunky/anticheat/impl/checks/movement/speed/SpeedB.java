package cc.funkemunky.anticheat.impl.checks.movement.speed;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.POSITION})
@Init
@CheckInfo(name = "Speed (Type B)", description = "Ensures air acceleration is legitimate.", type = CheckType.SPEED, maxVL = 75)
public class SpeedB extends Check {
    private double vl;
    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        double predicted = move.getLastDeltaXZ() * 0.91f;
        double delta = (move.getDeltaXZ() - predicted) * 38.2;

        if(move.getAirTicks() > 2 && !getData().isGeneralCancel() && move.getBlockAboveTicks() == 0 && move.getHalfBlockTicks() == 0 && move.getWebTicks() == 0 && move.getLiquidTicks() == 0 && move.getClimbTicks() == 0) {
            if(delta > 1.001) {
                if(vl++ > 3) {
                    flag("delta=" + delta, true, true, AlertTier.HIGH);
                }
            } else vl-= vl > 0 ? 0.5 : 0;
            debug("delta=" + delta + " vl=" + vl);
        } else vl-= vl > 0 ? 1 : 0;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
