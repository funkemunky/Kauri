package cc.funkemunky.anticheat.impl.checks.movement.fly;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Verbose;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import lombok.val;
import org.bukkit.event.Event;

@Init
@CheckInfo(name = "Fly (Type C)", description = "Ensures the user is not jumping higher than possible.", type = CheckType.FLY, maxVL = 35)
@Packets(packets = {Packet.Client.POSITION, Packet.Client.POSITION_LOOK})
public class FlyC extends Check {

    private Verbose verbose = new Verbose();
    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        float threshold =  MiscUtils.getPredictedJumpHeight(getData()) + 0.1f;
        if(!move.isCancelFlight() && verbose.flag(2, 1000L) && move.getDeltaY() > threshold) {
            flag(move.getDeltaY() + ">-" + threshold,
                    true,
                    true,
                    verbose.getVerbose() > 4 ? AlertTier.HIGH : AlertTier.LIKELY);
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
