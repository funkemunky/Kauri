package cc.funkemunky.anticheat.impl.checks.movement.motion;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.PlayerUtils;
import lombok.val;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffectType;

@CheckInfo(name = "Motion (Type B)", description = "Experimental instant movement check.", type = CheckType.MOTION, executable = false)
@Packets(packets = {Packet.Client.POSITION, Packet.Client.POSITION_LOOK, Packet.Client.LEGACY_POSITION_LOOK, Packet.Client.LEGACY_POSITION})
public class MotionB extends Check {
    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();
        val velocity = getData().getVelocityProcessor();
        val distance = move.getTo().toVector().distance(move.getFrom().toVector());
        val max = 2 + getJumpBoost() + Math.max(velocity.getMotionY(), 0);

        if(distance > max) {
            flag(distance + ">-"+ max, true, true, AlertTier.LIKELY);
        }

    }

    @Override
    public void onBukkitEvent(Event event) {

    }

    private double getJumpBoost() {
        return PlayerUtils.getPotionEffectLevel(getData().getPlayer(), PotionEffectType.JUMP) * 0.2f;
    }
}
