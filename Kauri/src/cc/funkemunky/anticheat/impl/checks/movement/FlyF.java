package cc.funkemunky.anticheat.impl.checks.movement;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.POSITION, Packet.Client.LEGACY_POSITION_LOOK, Packet.Client.LEGACY_POSITION})
public class FlyF extends Check {
    public FlyF(String name, CheckType type, CancelType cancelType, int maxVL) {
        super(name, type, cancelType, maxVL);
    }

    private long lastTimeStamp;
    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(MiscUtils.cancelForFlight(getData(), 8) || timeStamp < lastTimeStamp + 5) return;

        val move = getData().getMovementProcessor();

        if(move.getDeltaY() > move.getLastDeltaY() + 0.001f && move.getAirTicks() > 3 && !move.isNearGround(getData(), 1.5f)) {
            flag(move.getDeltaY() + ">-" + move.getLastDeltaY(), true, true);
        }
        lastTimeStamp = timeStamp;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
