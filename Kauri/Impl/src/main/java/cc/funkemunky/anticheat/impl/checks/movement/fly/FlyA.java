package cc.funkemunky.anticheat.impl.checks.movement.fly;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Verbose;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import lombok.val;
import org.bukkit.event.Event;

@Init
@Packets(packets = {Packet.Client.POSITION, Packet.Client.POSITION_LOOK})
@CheckInfo(name = "Fly (Type A)", description = "Ensures the acceleration of a player is legitimate.", type = CheckType.FLY, maxVL = 50)
public class FlyA extends Check {

    private Verbose verbose = new Verbose();

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(getData().isServerPos()) return;
        val move = getData().getMovementProcessor();

        if(!move.isServerOnGround() && !move.isNearGround() && move.getAirTicks() > 1 && !MiscUtils.cancelForFlight(getData(), 12, false)) {
            float predicted = (move.getLastDeltaY() - 0.08f) * 0.98f;

            if(Math.abs(predicted) < 0.005) {
                predicted = 0;
            }

            float delta = Math.abs(predicted - move.getDeltaY());

            if(delta > 1E-5) {
                if(verbose.flag(3, 500L)) {
                    flag("predicted=" + predicted + " deltaY=" + move.getDeltaY() + " delta=" + delta, true, true, AlertTier.HIGH);
                }
                debug(Color.Green + "Flagged: ");
            }

            debug("predicted=" + predicted + " deltaY=" + move.getDeltaY());
        }
    }


    @Override
    public void onBukkitEvent(Event event) {

    }
}
