package cc.funkemunky.anticheat.impl.checks.movement.motion;

import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import lombok.val;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

@CheckInfo(name = "Motion (Type B)", description = "Looks for impossible air modifications like strafe control.", type = CheckType.MOTION, maxVL = 80, developer = true)
@Packets(packets = {Packet.Client.POSITION, Packet.Client.POSITION_LOOK, Packet.Client.LEGACY_POSITION_LOOK, Packet.Client.LEGACY_POSITION})
@Init
public class MotionB extends Check {

    private int vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val action = getData().getActionProcessor();
        val move = getData().getMovementProcessor();
        val player = getData().getPlayer();

        if(!getData().isGeneralCancel() && getData().getLastServerPos().hasPassed(0)) {
            val direction = move.getTo().toLocation(player.getWorld()).getDirection();
            val moveVector = new Vector(move.getTo().getX() - move.getFrom().getX(), move.getTo().getY() - move.getFrom().getY(), move.getTo().getZ() - move.getFrom().getZ());

            val diff = direction.distance(moveVector);

            debug("diff=" + diff);
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
