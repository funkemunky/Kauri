package cc.funkemunky.anticheat.impl.checks.movement;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
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
public class FlyB extends Check {
    public FlyB(String name, String description, CheckType type, CancelType cancelType, int maxVL, boolean enabled, boolean executable, boolean cancellable) {
        super(name, description, type, cancelType, maxVL, enabled, executable, cancellable);
    }

    private int vl;
    private Verbose verbose = new Verbose();
    private long lastTimeStamp;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        if(MiscUtils.cancelForFlight(getData(), 30) || timeStamp < lastTimeStamp + 5) return;

        if(!move.isServerOnGround()
                && move.getDeltaY() > move.getServerYVelocity() + 0.001
                && !MathUtils.approxEquals(0.002, move.getServerYAcceleration(), move.getClientYAcceleration())) {
            if((!move.isNearGround() && move.getAirTicks() > 4 && verbose.flag(3, 500L)) || vl++ > 4)
            flag(move.getDeltaY() + ">-" + (move.getServerYVelocity() + 0.001), true, true);
        } else vl-= vl > 0 ? 1 : 0;
        debug("MOTIONY: " + MathUtils.round(move.getDeltaY(), 4) + " SERVERY: " + MathUtils.round(move.getServerYVelocity(), 4));
        lastTimeStamp = timeStamp;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
