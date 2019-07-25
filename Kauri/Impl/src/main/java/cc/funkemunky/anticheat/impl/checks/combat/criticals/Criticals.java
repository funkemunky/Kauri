package cc.funkemunky.anticheat.impl.checks.combat.criticals;

import cc.funkemunky.anticheat.api.checks.*;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import lombok.val;
import org.bukkit.event.Event;

@CheckInfo(name = "Criticals", type = CheckType.COMBAT, cancelType = CancelType.COMBAT, maxVL = 50)
@Init
@Packets(packets = {Packet.Client.POSITION, Packet.Client.POSITION_LOOK})
public class Criticals extends Check {

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        if(getData().getLastAttack().hasNotPassed(1) && !getData().isGeneralCancel() && move.getGroundTicks() > 10) {
            if(move.getDeltaY() < 0 && !move.isHalfBlocksAround() && !move.isBlocksOnTop()) {
                flag(move.getDeltaY() + "<-0", true, true, AlertTier.HIGH);
            }
            debug("deltaY=" + move.getDeltaY() + " ticks=" + move.getGroundTicks());
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
