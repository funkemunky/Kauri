package cc.funkemunky.anticheat.impl.checks.movement.speed;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import lombok.val;
import org.bukkit.event.Event;

@Init
@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.POSITION})
@CheckInfo(name = "Speed (Type D)", description = "Sets a maximum limit to how fast a player can move in a tick.", type = CheckType.SPEED, maxVL = 50, executable = true)
public class SpeedD extends Check {

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        val max = move.getBaseSpeed() + 1.1;

        if(move.getDeltaXZ() > max && !getData().isGeneralCancel() && !getData().takingVelocity(20)) {
            flag(move.getDeltaXZ() + ">-" + max, true, true, AlertTier.HIGH);
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
