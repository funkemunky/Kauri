package cc.funkemunky.anticheat.impl.checks.movement.fly;

import cc.funkemunky.anticheat.api.checks.*;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Verbose;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MathUtils;
import lombok.val;
import org.bukkit.event.Event;

@CheckInfo(name = "Fly (Type E)", description = "Detects flys.", type = CheckType.FLY, executable = false, cancelType = CancelType.MOTION)
@Init
@Packets(packets = {Packet.Client.LEGACY_POSITION, Packet.Client.LEGACY_POSITION_LOOK, Packet.Client.POSITION_LOOK, Packet.Client.POSITION})
public class FlyE extends Check {

    private Verbose verbose = new Verbose();

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        if (MiscUtils.cancelForFlight(getData(), -1, false)) return;

        val delta = MathUtils.getDelta(move.getDeltaY(), move.getServerYVelocity());

        val max = 0.02 + (getData().getVelocityProcessor().getLastVelocity().hasNotPassed(20) ? Math.max(getData().getVelocityProcessor().getMaxVertical(), 0.3) : 0);

        if(delta > max) {
            if(delta > 1 + max || verbose.flag(5, 500L)) {
                flag(move.getDeltaY() + ">-" + move.getServerYVelocity(), true, true, AlertTier.LIKELY);
            }
        }

        if(!move.isServerOnGround()) debug(move.getDeltaY() + ", " + move.getServerYVelocity());
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
