package cc.funkemunky.anticheat.impl.checks.movement;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.POSITION, Packet.Client.LEGACY_POSITION, Packet.Client.LEGACY_POSITION_LOOK})
public class StepA extends Check {
    public StepA(String name, String description, CheckType type, CancelType cancelType, int maxVL, boolean enabled, boolean executable, boolean cancellable) {
        super(name, description, type, cancelType, maxVL, enabled, executable, cancellable);

        setDeveloper(true);
    }

    float yTotal;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        debug(move.isServerOnGround() + "");
        if(getData().isGeneralCancel()) return;

        if(move.isCollidesHorizontally()) {
            debug("TOTAL: " + (yTotal+=move.getDeltaY()));
        } else if(yTotal % 1.0 == 0 && yTotal > 0) {
            flag("total:" + yTotal, true, true);
            yTotal = 0;
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
