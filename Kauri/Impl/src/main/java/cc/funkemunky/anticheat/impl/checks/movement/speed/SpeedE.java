package cc.funkemunky.anticheat.impl.checks.movement.speed;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MathUtils;
import lombok.val;
import org.bukkit.event.Event;

@Init
@CheckInfo(name = "Speed (Type E)", description = "Checks for instant movements which are impossible.", type = CheckType.SPEED, maxVL = 20)
@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.POSITION})
public class SpeedE extends Check {

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        val accel = MathUtils.getDelta(move.getDeltaXZ(), move.getLastDeltaXZ());
        float max = 0.6f + MiscUtils.getBaseSpeed(getData());

        max+= move.getBlockAboveTicks() > 0 ? 0.12 + (0.005 * Math.min(move.getIceTicks(), 120)) : 0;
        if(accel > max && !getData().isGeneralCancel() && !move.isServerPos() && !getData().isServerPos()) {
            flag(accel + ">-" + max, true, true, AlertTier.HIGH);
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
