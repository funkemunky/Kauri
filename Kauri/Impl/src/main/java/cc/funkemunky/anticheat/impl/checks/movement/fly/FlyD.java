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

import java.util.HashSet;
import java.util.Set;

@Init
@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.POSITION})
@CheckInfo(name = "Fly (Type D)", description = "Looks for repeating y values.", type = CheckType.FLY)
public class FlyD extends Check {

    private Set<Float> yValues = new HashSet<>();
    private int ticks, vl;
    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        if(!getData().isGeneralCancel() && getData().getLastBlockBreak().hasPassed(15) && !move.isServerOnGround() && getData().getLastBlockPlace().hasPassed(30) && move.getBlockAboveTicks() == 0 && move.getWebTicks() == 0 && move.getHalfBlockTicks() == 0 && move.getLiquidTicks() == 0 && move.getClimbTicks() == 0) {
            if(ticks++ >= 10) {
                int size = yValues.size();

                if(size < 5) {
                    if(vl++ > 1) {
                        flag(size + "-<8", true, true, AlertTier.HIGH);
                    }
                } else vl-= vl > 0 ? 1 : 0;
                debug("size=" + yValues.size());
                ticks = 0;
                yValues.clear();
            } else yValues.add(move.getDeltaY());
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
