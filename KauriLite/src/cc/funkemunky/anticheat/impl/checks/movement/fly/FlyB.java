package cc.funkemunky.anticheat.impl.checks.movement.fly;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Verbose;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.MathUtils;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {
        Packet.Client.POSITION_LOOK,
        Packet.Client.POSITION,
        Packet.Client.LEGACY_POSITION_LOOK,
        Packet.Client.LEGACY_POSITION})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "Fly (Type B)", description = "Calculates what the actual vertical speed of a player should be.", type = CheckType.FLY, cancelType = CancelType.MOTION, maxVL = 175, executable = false)
public class FlyB extends Check {
    public FlyB() {

    }

    private int vl;
    private Verbose verbose = new Verbose();
    private long lastTimeStamp;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        if (MiscUtils.cancelForFlight(getData(), 30, false) || timeStamp < lastTimeStamp + 5) return;

        if (!move.isServerOnGround()
                && move.getDeltaY() > move.getServerYVelocity() + 0.001
                && !MathUtils.approxEquals(0.002, move.getServerYAcceleration(), move.getClientYAcceleration())) {
            if ((!move.isNearGround() && move.getAirTicks() > 4 && verbose.flag(3, 500L)) || vl++ > 4)
                flag(move.getDeltaY() + ">-" + (move.getServerYVelocity() + 0.001), true, true);
        } else vl -= vl > 0 ? 1 : 0;
        debug("MOTIONY: " + MathUtils.round(move.getDeltaY(), 4) + " SERVERY: " + MathUtils.round(move.getServerYVelocity(), 4));
        lastTimeStamp = timeStamp;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
