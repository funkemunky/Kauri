package cc.funkemunky.anticheat.impl.checks.movement.fly;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import lombok.val;
import org.bukkit.event.Event;

@CheckInfo(name = "Fly (Type C)", description = "Looks for common behavior in yports.", type = CheckType.FLY, maxVL = 60)
@Packets(packets = {Packet.Client.POSITION, Packet.Client.POSITION_LOOK})
@Init
public class FlyC extends Check {

    private int vl;
    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        if(!getData().isGeneralCancel()) {
            double delta = Math.abs(move.getDeltaY() + move.getLastDeltaY());

            if(delta < 0.001 && getData().getLastBlockPlace().hasPassed(20) && Math.abs(move.getDeltaY()) > 0 && !move.isBlocksOnTop() && !move.isNearLiquid() && move.getWebTicks() == 0 && !move.isOnClimbable() && move.getHalfBlockTicks() == 0) {
                if((vl = Math.min(vl + 1, 5)) > 2) {
                    flag(move.getDeltaY() + ", " + move.getLastDeltaY(), true, true, AlertTier.HIGH);
                }
            } else vl-= vl > 0 ? 1 : 0;

            debug("delta=" + delta + " vl=" + vl);
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
